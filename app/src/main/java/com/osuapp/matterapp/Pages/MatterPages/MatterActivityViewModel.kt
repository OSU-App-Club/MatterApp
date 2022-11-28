package com.osuapp.matterapp.Pages.MatterPages

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.lifecycle.*
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import com.google.android.gms.home.matter.commissioning.DeviceInfo
import com.osuapp.matterapp.*
import com.osuapp.matterapp.shared.matter.chip.ClustersHelper
import com.osuapp.matterapp.shared.matter.commissioning.AppCommissioningService
import com.osuapp.matterapp.shared.matter.data.DevicesRepository
import com.osuapp.matterapp.shared.matter.data.DevicesStateRepository
import com.osuapp.matterapp.shared.matter.data.UserPreferencesRepository
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Encapsulates all of the information on a specific device. Note that the app currently only
 * supports Matter devices with server attribute "ON/OFF".
 */
data class DeviceUiModel(
    // Device information that is persisted in a Proto DataStore. See DevicesRepository.
    val device: Device,

    // Device state information that is retrieved dynamically.
    // Whether the device is online or offline.
    var isOnline: Boolean,
    // Whether the device is on or off.
    var isOn: Boolean
)

/**
 * UI model that encapsulates the information about the devices to be displayed on the Home screen.
 */
data class DevicesUiModel(
    // The list of devices.
    val devices: List<DeviceUiModel>,
    // Making it so default is false, so that codelabinfo is not shown when we have not gotten
    // the userpreferences data yet.
    val showCodelabInfo: Boolean,
    // Whether offline devices should be shown.
    val showOfflineDevices: Boolean
)

@HiltViewModel
internal class MatterActivityViewModel
@Inject
constructor(
    private val devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clustersHelper: ClustersHelper,
): ViewModel() {
    // Controls whether a periodic ping to the devices is enabled or not.
    private var devicesPeriodicPingEnabled: Boolean = true

    // MARK: - Get list of devices stored in account

    private val devicesFlow = devicesRepository.devicesFlow
    private val devicesStateFlow = devicesStateRepository.devicesStateFlow
    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    // Every time the list of devices or user preferences are updated (emit is triggered),
    // we recreate the DevicesUiModel
    private val devicesUiModelFlow =
        combine(devicesFlow, devicesStateFlow, userPreferencesFlow) {
                devices: Devices,
                devicesStates: DevicesState,
                userPreferences: UserPreferences ->
            Timber.d("*** devicesUiModelFlow changed ***")
            return@combine DevicesUiModel(
                devices = processDevices(devices, devicesStates, userPreferences),
                showCodelabInfo = false,//!userPreferences.hideCodelabInfo,
                showOfflineDevices = true)//!userPreferences.hideOfflineDevices)
        }

    val devicesUiModelLiveData = devicesUiModelFlow.asLiveData()

    private fun processDevices(
        devices: Devices,
        devicesStates: DevicesState,
        userPreferences: UserPreferences
    ): List<DeviceUiModel> {
        val devicesUiModel = ArrayList<DeviceUiModel>()
        devices.devicesList.forEach { device ->
            Timber.d("processDevices() deviceId: [${device.deviceId}]}")
            val state = devicesStates.devicesStateList.find { it.deviceId == device.deviceId }
            if (userPreferences.hideOfflineDevices) {
                if (state?.online != true) return@forEach
            }
            if (state == null) {
                Timber.d("    deviceId setting default value for state")
                devicesUiModel.add(DeviceUiModel(device, isOnline = false, isOn = false))
            } else {
                Timber.d("    deviceId setting its own value for state")
                devicesUiModel.add(DeviceUiModel(device, state.online, state.on))
            }
        }
        return devicesUiModel
    }

    // MARK: - Commissioning
    private val _commissionDeviceStatus = MutableLiveData<TaskStatus>(TaskStatus.NotStarted)
    val commissionDeviceStatus: LiveData<TaskStatus>
        get() = _commissionDeviceStatus

    private val _commissionDeviceIntentSender = MutableLiveData<IntentSender?>()
    val commissionDeviceIntentSender: LiveData<IntentSender?>
        get() = _commissionDeviceIntentSender

    // Called by the fragment in Step 5 of the Device Commissioning flow.
    fun commissionDeviceSucceeded(activityResult: ActivityResult, message: String) {
        val result =
            CommissioningResult.fromIntentSenderResult(activityResult.resultCode, activityResult.data)
        Timber.i("Device commissioned successfully! deviceName [${result.deviceName}]")
        Timber.i("Device commissioned successfully! room [${result.room}]")
        Timber.i(
            "Device commissioned successfully! DeviceDescriptor of device:\n" +
                    "deviceType [${result.commissionedDeviceDescriptor.deviceType}]\n" +
                    "productId [${result.commissionedDeviceDescriptor.productId}]\n" +
                    "vendorId [${result.commissionedDeviceDescriptor.vendorId}]\n" +
                    "hashCode [${result.commissionedDeviceDescriptor.hashCode()}]")

        // Update the data in the devices repository.
        viewModelScope.launch {
            try {
                val deviceId = result.token?.toLong()!!
                val currentDevice: Device = devicesRepository.getDevice(deviceId)
                val roomName =
                    result.room?.name // needed 'cause smartcast impossible with open/custom getter
                val updatedDeviceBuilder =
                    Device.newBuilder(currentDevice)
                        .setDeviceType(
                            convertToAppDeviceType(result.commissionedDeviceDescriptor.deviceType))
                        .setProductId(result.commissionedDeviceDescriptor.productId.toString())
                        .setVendorId(result.commissionedDeviceDescriptor.vendorId.toString())
                if (result.deviceName != null) updatedDeviceBuilder.name = result.deviceName
                if (roomName != null) updatedDeviceBuilder.room = roomName
                devicesRepository.updateDevice(updatedDeviceBuilder.build())
                // done: Zach - Uncomment code and resolve errors
                _commissionDeviceStatus.postValue(TaskStatus.Completed(message))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    // Called by the fragment in Step 5 of the Device Commissioning flow.
    fun commissionDeviceFailed(message: String) {
        // done: Zach - Uncomment code and resolve errors
        _commissionDeviceStatus.postValue(TaskStatus.Failed(Throwable(message)))
    }
    fun stopDevicesPeriodicPing() {
        devicesPeriodicPingEnabled = false
    }

    fun commissionDevice(intent: Intent, context: Context) {
        Timber.d("commissionDevice")
        _commissionDeviceStatus.postValue(TaskStatus.InProgress)

        val isMultiAdminCommissioning = isMultiAdminCommissioning(intent)

        val commissionRequestBuilder =
            CommissioningRequest.builder()
                .setCommissioningService(ComponentName(context, AppCommissioningService::class.java))
        if (isMultiAdminCommissioning) {
            val deviceName = intent.getStringExtra("com.google.android.gms.home.matter.EXTRA_DEVICE_NAME")
            commissionRequestBuilder.setDeviceNameHint(deviceName)

            val vendorId = intent.getIntExtra("com.google.android.gms.home.matter.EXTRA_VENDOR_ID", -1)
            val productId = intent.getIntExtra("com.google.android.gms.home.matter.EXTRA_PRODUCT_ID", -1)
            val deviceType =
                intent.getIntExtra("com.google.android.gms.home.matter.EXTRA_DEVICE_Type", -1)
            val deviceInfo = DeviceInfo.builder().setProductId(productId).setVendorId(vendorId).build()
            commissionRequestBuilder.setDeviceInfo(deviceInfo)

            val manualPairingCode =
                intent.getStringExtra("com.google.android.gms.home.matter.EXTRA_MANUAL_PAIRING_CODE")
            commissionRequestBuilder.setOnboardingPayload(manualPairingCode)
        }
        val commissioningRequest = commissionRequestBuilder.build()

        Matter.getCommissioningClient(context)
            .commissionDevice(commissioningRequest)
            .addOnSuccessListener { result ->
                // Communication with fragment is via livedata
                _commissionDeviceStatus.postValue(TaskStatus.InProgress)
                _commissionDeviceIntentSender.postValue(result)
            }
            .addOnFailureListener { error ->
                _commissionDeviceStatus.postValue(TaskStatus.Failed(error))
                Timber.e(error)
            }
    }

    fun startDevicesPeriodicPing() {
        Timber.d(
            "${LocalDateTime.now()} startDevicesPeriodicPing every $PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS seconds")
        devicesPeriodicPingEnabled = true
        runDevicesPeriodicPing()
    }

    private fun runDevicesPeriodicPing() {
        viewModelScope.launch {
            while (devicesPeriodicPingEnabled) {
                // For each ne of the real devices
                val devicesList = devicesRepository.getAllDevices().devicesList
                devicesList.forEach { device ->
                    if (device.name.startsWith(DUMMY_DEVICE_NAME_PREFIX)) {
                        return@forEach
                    }
                    Timber.d("runDevicesPeriodicPing deviceId [${device.deviceId}]")
                    var isOn = clustersHelper.getDeviceStateOnOffCluster(device.deviceId, 1)
                    val isOnline: Boolean
                    if (isOn == null) {
                        Timber.e("runDevicesPeriodicUpdate: flakiness with mDNS")
                        isOn = false
                        isOnline = false
                    } else {
                        isOnline = true
                    }
                    Timber.d("runDevicesPeriodicPing deviceId [${device.deviceId}] [${isOnline}] [${isOn}]")
                    // done: only need to do it if state has changed
                    devicesStateRepository.updateDeviceState(
                        device.deviceId, isOnline = isOnline, isOn = isOn
                    )
                }
                delay(PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS * 1000L)
            }
        }
    }

    // MARK: - State control
    fun updateDeviceStateOn(deviceUiModel: DeviceUiModel, isOn: Boolean) {
        Timber.d("updateDeviceStateOn: Device [${deviceUiModel}]  isOn [${isOn}]")
        viewModelScope.launch {
            if (isDummyDevice(deviceUiModel.device.name)) {
                Timber.d("Handling test device")
                devicesStateRepository.updateDeviceState(deviceUiModel.device.deviceId, true, isOn)
            } else {
                Timber.d("Handling real device")
                clustersHelper.setOnOffDeviceStateOnOffCluster(deviceUiModel.device.deviceId, isOn, 1)
                devicesStateRepository.updateDeviceState(deviceUiModel.device.deviceId, true, isOn)
            }
        }
    }
}
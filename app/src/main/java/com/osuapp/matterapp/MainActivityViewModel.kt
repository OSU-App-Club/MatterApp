package com.osuapp.matterapp

import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import com.osuapp.matterapp.chip.ClustersHelper
import com.osuapp.matterapp.data.DevicesRepository
import com.osuapp.matterapp.data.DevicesStateRepository
import com.osuapp.matterapp.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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
internal class MainActivityViewModel
@Inject
constructor(
    private val devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clustersHelper: ClustersHelper,
): ViewModel() {
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
                // TODO: Zach - Uncomment code and resolve errors
//                _commissionDeviceStatus.postValue(TaskStatus.Completed(message))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    // Called by the fragment in Step 5 of the Device Commissioning flow.
    fun commissionDeviceFailed(message: String) {
        // TODO: Zach - Uncomment code and resolve errors
//        _commissionDeviceStatus.postValue(TaskStatus.Failed(Throwable(message)))
    }



    // MARK: - State control

    fun updateDeviceStateOn(deviceUiModel: DeviceUiModel, isOn: Boolean) {
        // TODO: Andrew - Copy correct code into here
    }
}
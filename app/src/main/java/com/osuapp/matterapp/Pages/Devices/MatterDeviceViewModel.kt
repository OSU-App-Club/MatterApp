package com.osuapp.matterapp.Pages.Devices

import android.util.Log
import androidx.lifecycle.*
import com.osuapp.matterapp.Pages.MatterPages.DeviceUiModel
import com.osuapp.matterapp.R
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import shared.Models.Device
import shared.Utility.CatApi
import timber.log.Timber
import java.nio.file.Files.find


class MatterDeviceViewModel : ViewModel(), DefaultLifecycleObserver {
    private val _TAG = MatterDeviceViewModel::class.java.simpleName

    /** Encapsulated data **/
    private val _devices = MutableLiveData<List<DevicesListItem>>().apply {
        value = listOf()
    }

    private var devicesList: MutableList<Device> = mutableListOf()
    private var loadedList = false

    private val matterDevicesViewModelJob = Job()
    private var coroutineScope = CoroutineScope(matterDevicesViewModelJob + Dispatchers.Main)

    /** Public Accessors **/
    val devices : LiveData<List<DevicesListItem>> = _devices

    // Each Device List Entity
    class DevicesListItem {
        var id: String = ""
        var label: String = ""
        var state: Boolean = false
        var online: Boolean = false
        var image: Int = -1

        constructor(id: String, label: String, state: Boolean, online: Boolean, image: Int) {
            this.id = id
            this.label = label
            this.state = state
            this.online = online
            this.image = image
        }

        constructor()
    }

    /** Lifecycle Handlers **/
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        coroutineScope.launch {
            try {
                val awsHttpResponse = CatApi.retrofitService.getDevices().await()
                devicesList = mutableListOf<Device>()

                val data = JSONObject(awsHttpResponse).getJSONArray("devices")
                for ( i in 0 until data.length() ) {
                    try {
                        devicesList.add(Device(data.getJSONObject(i)))
                    } catch (e : Exception) {
                        Log.e(_TAG, "Parsing devices list error: $e")
                    }
                }

                Timber.i("Devices: $data")

                // Generate mutable DevicesListItem
                val mutableDevicesList = mutableListOf<DevicesListItem>()
                devicesList.forEach {
                    // TODO: The structure of DeviceListItem might be wrong?
                    mutableDevicesList.add(DevicesListItem(it.getDeviceId(), it.getDeviceName(), false, false, R.drawable.ic_baseline_outlet_24))
                }

                // Send event to update Devices View
                _devices.postValue(mutableDevicesList)

//                Timber.i("Devices: ${devices.value}")

                loadedList = true
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(_TAG, "Async getDevices failed: ${e.localizedMessage}")
            }
        }
    }

    fun updateDeviceStates(matterDevices: List<DeviceUiModel>) {
        coroutineScope.launch {
            while(!loadedList) {
                delay(1000)
            }

            for (matterDevice in matterDevices) {
                val device = _devices.value!!.find { it.id == matterDevice.device.deviceId.toString() }
                if (device != null) {
                    device.state = matterDevice.isOn
                    device.online = matterDevice.isOnline

//                    Timber.i("Device: ${matterDevice.device.deviceId}, State: ${matterDevice.isOn}, Online: ${matterDevice.isOnline}")
                }

                // update _devices
                _devices.postValue(_devices.value)
            }
        }
    }

    fun addDevice(matterDevices: List<DeviceUiModel>) {
        // check if a new device is added (find first missing device)
        coroutineScope.launch {
            while(!loadedList) {
                // sleep for 1 second
                withContext(Dispatchers.IO) {
                    Thread.sleep(1000)
                }
            }

            for (matterDevice in matterDevices) {
                // check if matterDevice.deviceId exists in _devices
                var found = false
                for (device in devicesList) {
                    if (device.getDeviceId() == matterDevice.device.deviceId.toString()) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    Timber.i("New device found: ${matterDevice.device.deviceId}")

                    // add device
                    coroutineScope.launch {
                        try {
                            // create json groups list
                            val list = JSONArray()
                            list.put("default")

                            // create Device json object
                            val json = JSONObject()
                            json.put("id", matterDevice.device.deviceId.toString())
                            json.put("name", matterDevice.device.name)
                            json.put("active", true)
                            json.put("wifi", matterDevice.device.room)
                            json.put("deviceType", matterDevice.device.deviceTypeValue.toString())    // TODO: map to string name
                            json.put("groups", list)

                            Timber.i("Sending device json: $json")

                            val body: RequestBody = RequestBody.create(MediaType.parse("application/json"), json.toString())

                            val awsHttpResponse = CatApi.retrofitService.addDevice(body).await()
                            // print response json
                            val response = JSONObject(awsHttpResponse)
                            Timber.i("Add device response: $response")

                            // add to devicesList
                            devicesList.add(Device(json))
                            val mutableDevicesList = mutableListOf<DevicesListItem>()
                            devicesList.forEach {
                                mutableDevicesList.add(DevicesListItem(it.getDeviceId(), it.getDeviceName(), matterDevice.isOn, matterDevice.isOnline, R.drawable.ic_std_device))
                            }

                            // Send event to update Devices View
                            _devices.postValue(mutableDevicesList)
                        } catch (e : Exception) {
                            e.printStackTrace()
                            Log.e(_TAG, "Async addDevice failed: ${e.localizedMessage}")
                        }
                    }
                    break
                }
            }
        }
    }
}
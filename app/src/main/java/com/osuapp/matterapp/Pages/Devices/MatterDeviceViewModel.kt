package com.osuapp.matterapp.Pages.Devices

import shared.Models.Device
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.osuapp.matterapp.Pages.MatterPages.DeviceUiModel
import com.osuapp.matterapp.Pages.MatterPages.DevicesUiModel
import com.osuapp.matterapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import shared.Utility.CatApi
import timber.log.Timber


class MatterDeviceViewModel : ViewModel(), DefaultLifecycleObserver {
    private val _TAG = MatterDeviceViewModel::class.java.simpleName

    /** Encapsulated data **/
    private val _devices = MutableLiveData<List<DevicesListItem>>().apply {
        value = listOf()
    }

    private var devicesList: MutableList<Device> = mutableListOf()

    private val matterDevicesViewModelJob = Job()
    private var coroutineScope = CoroutineScope(matterDevicesViewModelJob + Dispatchers.Main)

    /** Public Accessors **/
    val devices : LiveData<List<DevicesListItem>> = _devices

    // Each Device List Entity
    class DevicesListItem {
        var label: String = ""
        var image: Int = -1

        constructor(label: String, image: Int) {
            this.label = label
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
                    mutableDevicesList.add(DevicesListItem(it.getDeviceName(), R.drawable.ic_std_device))
                }

                // Send event to update Devices View
                _devices.postValue(mutableDevicesList)
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(_TAG, "Async getDevices failed: ${e.localizedMessage}")
            }
        }
    }

    fun addDevice(matterDevices: List<DeviceUiModel>) {
        // check if a new device is added (find first missing device)
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
                            mutableDevicesList.add(DevicesListItem(it.getDeviceName(), R.drawable.ic_std_device))
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
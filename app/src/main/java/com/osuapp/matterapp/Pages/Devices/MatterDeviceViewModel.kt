package com.osuapp.matterapp.Pages.Devices

import shared.Models.Device
import android.util.Log
import androidx.lifecycle.*
import com.osuapp.matterapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import shared.Utility.CatApi
import timber.log.Timber


class MatterDeviceViewModel : ViewModel(), DefaultLifecycleObserver {
    private val _TAG = MatterDeviceViewModel::class.java.simpleName

    /** Encapsulated data **/
    private val _devices = MutableLiveData<List<DevicesListItem>>().apply {
        value = listOf()
    }

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
                val devicesList = mutableListOf<Device>()

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

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }
}
package com.osuapp.matterapp.Pages.Groups

import android.util.Log
import androidx.lifecycle.*
import com.osuapp.matterapp.Pages.Devices.MatterDeviceViewModel
import com.osuapp.matterapp.Pages.MatterPages.DeviceUiModel
import com.osuapp.matterapp.R
import kotlinx.coroutines.*
import org.json.JSONObject
import shared.Models.Device
import shared.Utility.CatApi
import timber.log.Timber
import java.io.Serializable

class GroupsViewModel : ViewModel(), DefaultLifecycleObserver {
    private val _TAG = GroupsViewModel::class.java.simpleName

    /** Encapsulated data **/
    private val _devices = MutableLiveData<List<DevicesListItem>>().apply {
        value = listOf()
    }

    val groups = MutableLiveData<List<String>>().apply {
        value = listOf()
    }
    val selectedGroup = MutableLiveData<Int>().apply {
        value = 0
    }

    var devicesList: MutableList<Device> = mutableListOf()
    private var loadedList = false

    private val matterDevicesViewModelJob = Job()
    private var coroutineScope = CoroutineScope(matterDevicesViewModelJob + Dispatchers.Main)

    /** Public Accessors **/
    val devices : LiveData<List<DevicesListItem>> = _devices

    // Each Device List Entity
    class DevicesListItem : Serializable {
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
                    mutableDevicesList.add(DevicesListItem(it.getDeviceId(), it.getDeviceName(), false, false, R.drawable.ic_std_device))
                }

                // Send event to update Devices View
//                _devices.postValue(mutableDevicesList)

//                Timber.i("Devices: ${devices.value}")

                // get list of groups from deviceList
                var groupList = mutableListOf<String>()
                devicesList.forEach {
                    // merge with existing list
                    groupList.addAll(it.getDeviceGroups())
                }
                // remove duplicates
                groupList = groupList.distinct().toMutableList()
                // send event to update Groups View
                groups.postValue(groupList)

                if (selectedGroup.value!! >= groupList.size) {
                    selectedGroup.postValue(0)
                } else {
                    selectedGroup.postValue(selectedGroup.value!!)
                }

                getDevicesByGroup(groupList[selectedGroup.value!!])

                loadedList = true
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(_TAG, "Async getDevices failed: ${e.localizedMessage}")
            }
        }
    }

    fun getDevicesByGroup(group: String) {
        selectedGroup.postValue(groups.value?.indexOf(group))

        val devices = mutableListOf<DevicesListItem>()
        devicesList.forEach {
            if (it.getDeviceGroups().contains(group)) {
                devices.add(DevicesListItem(it.getDeviceId(), it.getDeviceName(), false, false, R.drawable.ic_baseline_outlet_24))
            }
        }
        _devices.postValue(devices)
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
                }

                // update _devices
                _devices.postValue(_devices.value)
            }
        }
    }
}
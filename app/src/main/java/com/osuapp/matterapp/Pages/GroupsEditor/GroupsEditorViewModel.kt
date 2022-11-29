package com.osuapp.matterapp.Pages.GroupsEditor

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import shared.Models.Device
import shared.Utility.CatApi
import timber.log.Timber
import java.io.Serializable

class GroupsEditorViewModel : ViewModel(), DefaultLifecycleObserver {

    val initialGroupName = MutableLiveData<String>().apply {
        value = ""
    }
    val currentGroupName = MutableLiveData<String>().apply {
        value = ""
    }

    val initialDevices = MutableLiveData<List<Device>>().apply {
        value = listOf()
    }
    val currentDevices = MutableLiveData<List<Device>>().apply {
        value = listOf()
    }
    var devicesInGroup = MutableLiveData<MutableList<DevicesListItem>>()

    private val matterDevicesViewModelJob = Job()
    private var coroutineScope = CoroutineScope(matterDevicesViewModelJob + Dispatchers.Main)

    class DevicesListItem : Serializable {
        var id: String = ""
        var name: String = ""

        constructor(id: String, name: String) {
            this.id = id
            this.name = name
        }

        constructor()
    }

    fun setDevicesInGroup(allDevices: List<Device>) {
        // add device to list if group name matches
        val devicesList = mutableListOf<DevicesListItem>()
        for (device in allDevices) {
            if (device.getDeviceGroups().contains(currentGroupName.value)) {
                devicesList.add(DevicesListItem(device.getDeviceId(), device.getDeviceName()))
            }
        }
        this.devicesInGroup.value = devicesList
    }

    fun onDeviceRemove(deviceId: String) {
        // Remove from displayed list
        val deviceToRemove: DevicesListItem? = devicesInGroup.value!!.find { d -> d.id == deviceId }!!
        val newList = devicesInGroup.value
        newList!!.remove(deviceToRemove)
        devicesInGroup.postValue(newList)

        Timber.i("Size: ${devicesInGroup.value!!.size}")
        for(device in devicesInGroup.value!!) {
            Timber.i("Device in group: ${device.id}, ${device.name}")
        }

        // Remove groupName from Device instance
        val device = currentDevices.value!!.find { d -> d.getDeviceId() == deviceId }!!
        val deviceGroups: MutableList<String> = device.getDeviceGroups()
        deviceGroups.remove(currentGroupName.value)
        device.setDeviceGroups(deviceGroups.toTypedArray())
    }

    fun save() {
        for (device in initialDevices.value!!) {
            coroutineScope.launch {
                try {
                    val deviceId = device.getDeviceId()
                    val initialGroups = device.getDeviceGroups()
                    val currentGroups = currentDevices.value!!.find { it.getDeviceId() == deviceId }!!.getDeviceGroups()

                    // check if groups are different
//                    if (initialGroups != currentGroups || initialGroupName.value != currentGroupName.value) {
                        // convert groups to json array
                        val jsonGroups = JSONArray()
                        for (group in currentGroups) {
                            if (group == initialGroupName.value) {
                                jsonGroups.put(currentGroupName.value)
                            } else {
                                jsonGroups.put(group)
                            }
                        }

                        Timber.i("initialGroupName: ${initialGroupName.value}")
                        Timber.i("currentGroupName: ${currentGroupName.value}")
                        Timber.i("saving groups: $jsonGroups")

                        val json = JSONObject()
                        json.put("id", deviceId)
                        json.put("groups", jsonGroups)
                        val body: RequestBody = RequestBody.create(MediaType.parse("application/json"), json.toString())
                        val awsHttpResponse = CatApi.retrofitService.updateDeviceById(deviceId, body).await()
                        Timber.i( "Async updateDeviceById success: $awsHttpResponse")
//                    }
                } catch (e : Exception) {
                    e.printStackTrace()
                    Timber.e( "Async updateDeviceById failed: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteGroup() {
        for (device in initialDevices.value!!) {
            coroutineScope.launch {
                try {
                    val deviceId = device.getDeviceId()
                    val initialGroups = device.getDeviceGroups()

                    if (initialGroups.contains(initialGroupName.value)) {
                        // convert groups to json array
                        val jsonGroups = JSONArray()
                        for (group in initialGroups) {
                            if (group != initialGroupName.value) {
                                jsonGroups.put(group)
                            }
                        }

                        val json = JSONObject()
                        json.put("id", deviceId)
                        json.put("groups", jsonGroups)
                        val body: RequestBody = RequestBody.create(MediaType.parse("application/json"), json.toString())
                        val awsHttpResponse = CatApi.retrofitService.updateDeviceById(deviceId, body).await()
                        Timber.i( "Async updateDeviceById success: $awsHttpResponse")
                    }
                } catch (e : Exception) {
                    e.printStackTrace()
                    Timber.e( "Async updateDeviceById failed: ${e.localizedMessage}")
                }
            }
        }
    }
}
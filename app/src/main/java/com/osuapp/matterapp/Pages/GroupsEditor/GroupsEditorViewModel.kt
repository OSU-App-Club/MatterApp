package com.osuapp.matterapp.Pages.GroupsEditor

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
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

    val groupName = MutableLiveData<String>().apply {
        value = ""
    }

    val initialDevices = MutableLiveData<List<Device>>().apply {
        value = listOf()
    }
    val currentDevices = MutableLiveData<List<Device>>().apply {
        value = listOf()
    }
    val devicesInGroup = MutableLiveData<MutableList<DevicesListItem>>()

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
        currentDevices.value = allDevices
        // add device to list if group name matches
        val devicesList = mutableListOf<DevicesListItem>()
        for (device in allDevices) {
            if (device.getDeviceGroups().contains(groupName.value)) {
                devicesList.add(DevicesListItem(device.getDeviceId(), device.getDeviceName()))
            }
        }
        this.devicesInGroup.value = devicesList
    }

    fun removeGroupFromDevices(allDevices: List<Device>) {
        for (device in allDevices) {


            // if device does not have groupName in list of Groups
            val deviceInGroup: Boolean =device.getDeviceGroups().contains(groupName.value)
            // if device (still) included in the list of devicesInGroup
            val deviceIncludedInList: Boolean = devicesInGroup.value!!.any { groupedDevice -> groupedDevice.id == device.getDeviceId() }

            Timber.i("groups: ${device.getDeviceGroups()}, $deviceInGroup, $deviceIncludedInList")

            if (!deviceInGroup) continue // Skip this device
            if (deviceIncludedInList) continue // Skip this device

            // Else, device is a member of group but was removed from the list by the user
            // So, remove groupName from device's list of groups
            val deviceGroups: MutableList<String> = device.getDeviceGroups()
            deviceGroups.remove(groupName.value)
            device.setDeviceGroups(deviceGroups.toTypedArray())

            // update devices.value
            val devicesList = currentDevices.value!!.toMutableList()
            val index = devicesList.indexOfFirst { it.getDeviceId() == device.getDeviceId() }
            devicesList[index] = device
            currentDevices.value = devicesList
        }
    }

    fun save() {
        for (device in initialDevices.value!!) {
            val lastGroups = device.getDeviceGroups()

            // check if device.getDeviceId() exists in deviceList.value
            val deviceListItem = devicesInGroup.value!!.find { deviceListItem -> deviceListItem.id == device.getDeviceId() }
            if (deviceListItem != null) {
                // add group to device
                val deviceGroups: MutableList<String> = device.getDeviceGroups()
                deviceGroups.add(groupName.value!!)
                device.setDeviceGroups(deviceGroups.toTypedArray())
            } else {
                // remove group from device
                val deviceGroups: MutableList<String> = device.getDeviceGroups()
                deviceGroups.remove(groupName.value!!)
                device.setDeviceGroups(deviceGroups.toTypedArray())
            }

            if (lastGroups != device.getDeviceGroups()) {
                // update device
                val deviceJson = JSONObject(Gson().toJson(device))
                val requestBody = RequestBody.create(MediaType.parse("application/json"), deviceJson.toString())
                coroutineScope.launch {
                    val response = CatApi.retrofitService.updateDeviceById(device.getDeviceId(), requestBody).await()
                    if (response.isSuccessful) {
                        Log.i("GroupsEditorViewModel", "updateDeviceById success")
                    } else {
                        Log.i("GroupsEditorViewModel", "updateDeviceById failed")
                    }
                }
            }
        }
    }

    fun deleteGroup() {
        for (device in initialDevices.value!!) {
            if (device.getDeviceGroups().contains(groupName.value)) {
                coroutineScope.launch {
                    try {
                        val deviceId = device.getDeviceId()
                        val groups = initialDevices.value!!.find { it.getDeviceId() == deviceId }!!
                            .getDeviceGroups()
                        groups.remove(groupName.value!!)
                        groups.distinct()

                        // convert groups to json array
                        val jsonGroups = JSONArray()
                        for (group in groups) {
                            jsonGroups.put(group)
                        }

                        val json = JSONObject()
                        json.put("id", deviceId)
                        json.put("groups", jsonGroups)
                        val body: RequestBody =
                            RequestBody.create(MediaType.parse("application/json"), json.toString())
                        val awsHttpResponse =
                            CatApi.retrofitService.updateDeviceById(deviceId, body).await()
                        Timber.i("Async updateDeviceById success: $awsHttpResponse")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Timber.e("Async updateDeviceById failed: ${e.localizedMessage}")
                    }
                }
            }
        }
    }
}
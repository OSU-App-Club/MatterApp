package com.osuapp.matterapp.Pages.GroupsEditorPart2

import androidx.lifecycle.*
import shared.Models.Device
import timber.log.Timber
import java.io.Serializable

class GroupsEditorViewModelPart2 : ViewModel(), DefaultLifecycleObserver {

    val groupName = MutableLiveData<String>().apply {
        value = ""
    }
    val devicesNotInGroup = MutableLiveData<List<DevicesListItem>>()

    class DevicesListItem : Serializable {
        var id: String = ""
        var name: String = ""
        var include: Boolean = false

        constructor(id: String, name: String) {
            this.id = id
            this.name = name
        }

        constructor()
    }

    fun setDevicesNotInGroup(allDevices: List<Device>) {
        // add device to list if group name matches
        val filteredDevicesList = mutableListOf<DevicesListItem>()
        for (device in allDevices) {
            if (!device.getDeviceGroups().contains(groupName.value)) {
                filteredDevicesList.add(DevicesListItem(device.getDeviceId(), device.getDeviceName()))
            }
        }
        this.devicesNotInGroup.value = filteredDevicesList
    }

    fun addGroupToIncludedDevices(allDevices: List<Device>) {
        for (deviceItem in this.devicesNotInGroup.value!!) {
            Timber.i("deviceItem include: ${deviceItem.include}")
            if (deviceItem.include) {
                val device: Device? =
                    allDevices.find { device -> device.getDeviceId() == deviceItem.id }
                if (device == null) continue
                val deviceGroups: MutableList<String> = device.getDeviceGroups()
                deviceGroups.add(groupName.value!!)
                device.setDeviceGroups(deviceGroups.toTypedArray())
            }
        }
    }
}
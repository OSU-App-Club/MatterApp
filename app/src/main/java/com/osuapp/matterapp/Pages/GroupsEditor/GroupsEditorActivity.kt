package com.osuapp.matterapp.Pages.GroupsEditor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osuapp.matterapp.Pages.GroupsEditorPart2.GroupsEditorActivityPart2
import com.osuapp.matterapp.R
import com.osuapp.matterapp.databinding.FragmentGroupsEditorBinding
import shared.Models.Device
import timber.log.Timber
import java.io.Serializable

class GroupsEditorActivity : AppCompatActivity() {

    private lateinit var _binding: FragmentGroupsEditorBinding

    private lateinit var groupsEditorViewModel: GroupsEditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = FragmentGroupsEditorBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        groupsEditorViewModel = ViewModelProvider(this).get(GroupsEditorViewModel::class.java)
        lifecycle.addObserver(groupsEditorViewModel)

        val extras: Bundle? = intent.extras
        val groupName = extras?.getString("groupName")
        var devices : List<Device> = extras?.getSerializable("devices") as List<Device>
        groupsEditorViewModel.initialDevices.value = devices
        groupsEditorViewModel.currentDevices.value = devices

//        for (device in devices) {
//            Timber.i("Device rcvd: ${device.id}, ${device.name}")
//        }
        groupsEditorViewModel.groupName.value = groupName

        // update text
        val name = _binding.nameFieldLayout
        name.editText?.setText(groupName)

        // prepare the recyclerview
        val recyclerView: RecyclerView = _binding.deviceList
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.adapter = GroupsEditorAdapter(mutableListOf())

        groupsEditorViewModel.devicesInGroup.observe(this) {
//            for (device in it) {
//                // update groupsEditorViewModel.currentDevices to remove this group
//                val currentDevice = groupsEditorViewModel.currentDevices.value?.find { it.getDeviceId() == device.id }
//                val newGroups = currentDevice?.getDeviceGroups()
//                newGroups?.remove(groupName)
//                currentDevice?.setDeviceGroups(newGroups?.toTypedArray()!!)
//            }

            recyclerView.adapter = GroupsEditorAdapter(it)
        }

        groupsEditorViewModel.setDevicesInGroup(devices)

        // add button
        val addBtn = _binding.addDeviceToGroupBtn
        addBtn.setOnClickListener {
//            for (device in devices) {
//                Timber.i("Device rcvd: ${device.getDeviceGroups()}, ${device.getDeviceId()}")
//            }
            groupsEditorViewModel.removeGroupFromDevices(devices)
//            for (device in devices) {
//                Timber.i("Device rcvd: ${device.getDeviceGroups()}, ${device.getDeviceId()}")
//            }

            val intent = Intent(this, GroupsEditorActivityPart2::class.java)
            intent.putExtra("groupName", groupName)
            intent.putExtra("devices", groupsEditorViewModel.currentDevices.value as Serializable)
            startActivityForResult(intent, 0)

            // After activity, update allDevices list (so have new groups)
//            devices = intent.getSerializableExtra("devices") as List<Device>
        }

        // cancel button
        val cancelBtn = _binding.cancelBtn
        cancelBtn.setOnClickListener {
            finish()
        }

        // save button
        val saveBtn = _binding.saveBtn
        saveBtn.setOnClickListener {
            groupsEditorViewModel.removeGroupFromDevices(devices)
            groupsEditorViewModel.save()
            finish()
        }
    }

    // get the result from the second activity
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Timber.i("Result OK")
                val devices = data?.getSerializableExtra("devices") as List<Device>
                for (device in devices) {
                    Timber.i("Device rcvd: ${device.getDeviceId()}, ${device.getDeviceGroups()}")
                }
                groupsEditorViewModel.setDevicesInGroup(devices)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.groups_editor_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.delete_group -> {
                groupsEditorViewModel.deleteGroup()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
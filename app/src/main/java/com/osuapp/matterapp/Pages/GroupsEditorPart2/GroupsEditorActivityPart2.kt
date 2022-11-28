package com.osuapp.matterapp.Pages.GroupsEditorPart2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.osuapp.matterapp.databinding.FragmentGroupsEditorPart2Binding
import shared.Models.Device
import java.io.Serializable


class GroupsEditorActivityPart2 : AppCompatActivity() {

    private lateinit var _binding: FragmentGroupsEditorPart2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = FragmentGroupsEditorPart2Binding.inflate(layoutInflater)
        setContentView(_binding.root)

        val groupsEditorViewModel = ViewModelProvider(this).get(GroupsEditorViewModelPart2::class.java)
        lifecycle.addObserver(groupsEditorViewModel)

        val extras: Bundle? = intent.extras
        val groupName = extras?.getString("groupName")
        val allDevices : List<Device> = extras?.getSerializable("devices") as List<Device>


        groupsEditorViewModel.groupName.value = groupName

        // prepare the recyclerview
        val recyclerView: RecyclerView = _binding.deviceList
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.adapter = GroupsEditorAdapterPart2(listOf())

        groupsEditorViewModel.devicesNotInGroup.observe(this) {
            recyclerView.adapter = GroupsEditorAdapterPart2(it)
        }

        groupsEditorViewModel.setDevicesNotInGroup(allDevices)


        // cancel button
        val cancelBtn = _binding.cancelBtn
        cancelBtn.setOnClickListener {
            finish()
        }

        // save button
        val saveBtn = _binding.saveBtn
        saveBtn.setOnClickListener {
            // Add group name to all selected devices
            groupsEditorViewModel.addGroupToIncludedDevices(allDevices)
            // return allDevices back to caller
            val returnIntent = Intent()
            returnIntent.putExtra("devices", allDevices as Serializable)
            setResult(RESULT_OK, returnIntent)

            finish()
        }
    }

}
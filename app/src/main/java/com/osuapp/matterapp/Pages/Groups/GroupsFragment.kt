package com.osuapp.matterapp.Pages.Groups

import android.R
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.osuapp.matterapp.Pages.Devices.MatterDevicesAdapter
import com.osuapp.matterapp.Pages.GroupsEditor.GroupsEditorActivity
import com.osuapp.matterapp.Pages.MatterPages.DevicesUiModel
import com.osuapp.matterapp.Pages.MatterPages.MatterActivityViewModel
import com.osuapp.matterapp.databinding.FragmentGroupsBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.Serializable
import java.security.acl.Group

@AndroidEntryPoint
class GroupsFragment : Fragment() {

    private lateinit var _binding: FragmentGroupsBinding

    private val viewModel: MatterActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val matterGroupsViewModel =
            ViewModelProvider(this).get(GroupsViewModel::class.java)
        lifecycle.addObserver(matterGroupsViewModel)

        _binding = FragmentGroupsBinding.inflate(inflater, container, false)

        val root: View = _binding.root

        val editGroupBtn = _binding.editGroupBtn
        editGroupBtn.setOnClickListener {
            val intent = Intent(context, GroupsEditorActivity::class.java)

            val selectedGroupTxt: String = matterGroupsViewModel.groups.value?.get(matterGroupsViewModel.selectedGroup.value!!)
                .toString()

            intent.putExtra("groupName", selectedGroupTxt)
            intent.putExtra("devices", matterGroupsViewModel.devicesList as Serializable)
            startActivity(intent)
        }

        val groupsSpinner = _binding.groupsSpinner
        val groups = listOf<String>() // arrayOf("Group 1", "Group 2", "Group 3", "Group 4", "Group 5")
        val groupsSpinnerAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, groups)
        groupsSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        groupsSpinner.adapter = groupsSpinnerAdapter

        groupsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // On selecting a spinner item
                val item = parent.getItemAtPosition(position).toString()
                // Showing selected spinner item
//                Toast.makeText(parent.context, "Selected: $item", Toast.LENGTH_LONG).show()

                matterGroupsViewModel.getDevicesByGroup(item)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        matterGroupsViewModel.selectedGroup.observe(viewLifecycleOwner) {
            groupsSpinner.setSelection(it)
        }

        /** Groups List Recycle View **/
        val devicesListRecyclerView = _binding.groupList

        // Setup recycler view for the devices list
        devicesListRecyclerView.layoutManager = GridLayoutManager(context, 3)
        devicesListRecyclerView.adapter = GroupsAdapter(listOf())

        matterGroupsViewModel.devices.observe(viewLifecycleOwner) { devices ->
            devicesListRecyclerView.adapter = GroupsAdapter(devices)
        }

        // update groups spinner
        matterGroupsViewModel.groups.observe(viewLifecycleOwner) {
            val groupsSpinnerAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, it)
            groupsSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            groupsSpinner.adapter = groupsSpinnerAdapter

            editGroupBtn.isEnabled = it.isNotEmpty()
        }

        // add group button
        val addGroupBtn: FloatingActionButton = _binding.addGroupBtn
        addGroupBtn.setOnClickListener {
            val intent = Intent(context, GroupsEditorActivity::class.java)
            intent.putExtra("groupName", "New Group")
            intent.putExtra("devices", matterGroupsViewModel.devicesList as Serializable)
            startActivity(intent)
        }

        // Matter
        // Observe the devicesLiveData.
        viewModel.devicesUiModelLiveData.observe(viewLifecycleOwner) { devicesUiModel: DevicesUiModel ->
            matterGroupsViewModel.updateDeviceStates(devicesUiModel.devices)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}
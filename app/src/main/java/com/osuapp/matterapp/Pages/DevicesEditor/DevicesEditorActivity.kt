package com.osuapp.matterapp.Pages.DevicesEditor

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.osuapp.matterapp.PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS
import com.osuapp.matterapp.Pages.Devices.MatterDeviceViewModel
import com.osuapp.matterapp.Pages.Groups.GroupsAdapter
import com.osuapp.matterapp.Pages.MatterPages.DeviceUiModel
import com.osuapp.matterapp.Pages.MatterPages.DevicesUiModel
import com.osuapp.matterapp.Pages.MatterPages.MatterActivityViewModel
import com.osuapp.matterapp.R
import com.osuapp.matterapp.TaskStatus
import com.osuapp.matterapp.databinding.FragmentDevicesEditorBinding
import com.osuapp.matterapp.isMultiAdminCommissioning
import dagger.hilt.android.AndroidEntryPoint
import shared.Models.Device
import timber.log.Timber


@AndroidEntryPoint
class DevicesEditorActivity : AppCompatActivity() {
    private val _TAG = MatterDeviceViewModel::class.java.simpleName

    private lateinit var _binding: FragmentDevicesEditorBinding

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding

    private val viewModel: MatterActivityViewModel by viewModels()

    private lateinit var deviceId: String
    private lateinit var devicesEditorViewModel: DevicesEditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = FragmentDevicesEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devicesEditorViewModel = ViewModelProvider(this).get(DevicesEditorViewModel::class.java)
        lifecycle.addObserver(devicesEditorViewModel)

        val textView = binding.nameFieldLayout

        devicesEditorViewModel.text.observe(this) {
            // set the text of the text view to the value of the text property
            textView.editText?.setText(it)
        }

        /* Initialize Data */
        val extras: Bundle? = intent.extras
        if(extras == null)
            return pageLoadFail("No extras passed to page")
        deviceId = extras.getString("deviceId").toString()
        Timber.i("Device ID: $deviceId")
        if(deviceId == null)
            return pageLoadFail("No deviceId extra found")

        var initDevice: Device? = null
        devicesEditorViewModel.getDevice(deviceId) { device ->
            initDevice = device
            if(initDevice == null)
                pageLoadFail("Failed to initialize device with id $deviceId")

            initDevice?.let { displayDeviceDetails(it) }
        } //this param should be passed to this Fragment on page call

        /* Cancel Button */
        val cancelBtn: Button = binding.cancelBtn
        cancelBtn.setOnClickListener {
            goToPreviousPage()
        }

        /* Save Button */
        val saveDeviceBtn: Button = binding.saveBtn
        saveDeviceBtn.setOnClickListener() {
            val updatedDevice: Device = readDeviceDetails(initDevice!!)
            devicesEditorViewModel.updateDevice(updatedDevice)
            goToPreviousPage()
        }

        /* Delete Button */
//        val deleteDeviceBtn: Button = binding.deleteDeviceBtn
//        deleteDeviceBtn.setOnClickListener() {
//            devicesEditorViewModel.deleteDevice(deviceId)
//            goToPreviousPage()
//        }

        var deviceUiModel: DeviceUiModel? = null

        // matter device list
        viewModel.devicesUiModelLiveData.observe(this) { devicesUiModel: DevicesUiModel ->
            for (device in devicesUiModel.devices) {
                if (device.device.deviceId.toString() == deviceId) {
                    deviceUiModel = device
                }
            }
        }

        // on button
        val onBtn: Button = binding.onBtn
        onBtn.setOnClickListener() {
            deviceUiModel?.let {
                viewModel.updateDeviceStateOn(it, true)
            }
        }

        // off button
        val offBtn: Button = binding.offBtn
        offBtn.setOnClickListener() {
            deviceUiModel?.let {
                viewModel.updateDeviceStateOn(it, false)
            }
        }

        // groups
        val groupsList = binding.groupList
        groupsList.layoutManager = GridLayoutManager(this, 1)
        groupsList.adapter = DeviceEditorGroupsAdapter(listOf())

        devicesEditorViewModel.groups.observe(this) {
            groupsList.adapter = DeviceEditorGroupsAdapter(it)
        }

        // schedules
        val scheduleList = binding.scheduleList
        scheduleList.layoutManager = GridLayoutManager(this, 1)
        scheduleList.adapter = DeviceEditorGroupsAdapter(listOf())

        devicesEditorViewModel.schedules.observe(this) {
            scheduleList.adapter = DevicesEditorScheduleAdapter(it)
        }
    }

    override fun onResume() {
        super.onResume()

        Timber.d("*** Main ***")
        if (PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS != -1) {
            Timber.d("Starting periodic ping on devices")
            viewModel.startDevicesPeriodicPing()
        }
    }

    override fun onPause() {
        super.onPause()

        Timber.d("onPause(): Stopping periodic ping on devices")
        viewModel.stopDevicesPeriodicPing()
    }

    private fun pageLoadFail(msg: String) {
        Log.e(_TAG, msg)
        goToPreviousPage()
    }

    private fun goToPreviousPage() {
//        val intent = Intent(this, MatterDeviceFragment::class.java)
//        startActivity(intent)
        finish()
    }

    private fun displayDeviceDetails(device: Device) {
        /* Name Attribute */
        val name: String = device.getDeviceName()
        binding.nameFieldLayout.editText?.setText(name)

        /* Groups Attribute */
//        val groups: MutableList<String> = device.getDeviceGroups()
//        val groupAdapter: ArrayAdapter<String> = ArrayAdapter(
//            this,
//            android.R.layout.simple_list_item_1,
//            groups
//        )
//        binding.groupDropDownBtn.adapter = groupAdapter


    }

    private fun readDeviceDetails(device: Device): Device {
        /* Name Attribute */
        val name: String = binding.nameFieldLayout.editText?.text.toString()
        device.setDeviceName(name)

        /* Groups Attribute */
//        val groupAdapter = binding.groupDropDownBtn.adapter
//        val count = groupAdapter.count
//        val groups: ArrayList<String> = ArrayList()
//        for (i in 0 until count) {
//            groups.add(groupAdapter.getItem(i) as String)
//        }

        return device
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.devices_editor_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.delete_device -> {
                viewModel.removeDevice(deviceId.toLong())
                devicesEditorViewModel.deleteDevice(deviceId)
                goToPreviousPage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
package com.osuapp.matterapp.Pages.DevicesEditor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.osuapp.matterapp.Pages.Devices.MatterDeviceFragment
import com.osuapp.matterapp.Pages.Devices.MatterDeviceViewModel
import com.osuapp.matterapp.databinding.FragmentDevicesEditorBinding
import shared.Models.Device
import timber.log.Timber

class DevicesEditorActivity : AppCompatActivity() {
    private val _TAG = MatterDeviceViewModel::class.java.simpleName

    private lateinit var _binding: FragmentDevicesEditorBinding

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = FragmentDevicesEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val devicesEditorViewModel = ViewModelProvider(this).get(DevicesEditorViewModel::class.java)
        lifecycle.addObserver(devicesEditorViewModel)

        val textView: TextView = binding.devicesEditorTextHeader

        devicesEditorViewModel.text.observe(this) {
            textView.text = it
        }

        /* Initialize Data */
        val extras: Bundle? = intent.extras
        if(extras == null)
            return pageLoadFail("No extras passed to page")
        val deviceId: String? = extras.getString("deviceId")
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
        val deleteDeviceBtn: Button = binding.deleteDeviceBtn
        deleteDeviceBtn.setOnClickListener() {
            devicesEditorViewModel.deleteDevice(deviceId)
            goToPreviousPage()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
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
        binding.devicesEditorTextHeader.text = name

        /* Groups Attribute */
        val groups: MutableList<String> = device.getDeviceGroups()
        val groupAdapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            groups
        )
        binding.groupDropDownBtn.adapter = groupAdapter
    }

    private fun readDeviceDetails(device: Device): Device {
        /* Name Attribute */
        val name: String = binding.devicesEditorTextHeader.text as String
        device.setDeviceName(name)

        /* Groups Attribute */
        val groupAdapter = binding.groupDropDownBtn.adapter
        val count = groupAdapter.count
        val groups: ArrayList<String> = ArrayList()
        for (i in 0 until count) {
            groups.add(groupAdapter.getItem(i) as String)
        }

        return device
    }
}
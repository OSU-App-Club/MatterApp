package com.osuapp.matterapp.Pages.Devices

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.osuapp.matterapp.PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS
import com.osuapp.matterapp.Pages.MatterPages.DevicesUiModel
import com.osuapp.matterapp.Pages.MatterPages.MatterActivityViewModel
import com.osuapp.matterapp.TaskStatus
import com.osuapp.matterapp.databinding.FragmentMatterDeviceBinding
import com.osuapp.matterapp.isMultiAdminCommissioning
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MatterDeviceFragment : Fragment() {

    private lateinit var _binding: FragmentMatterDeviceBinding

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding

    // The ActivityResult launcher that launches the "commissionDevice" activity in Google Play services.
    private lateinit var commissionDeviceLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val viewModel: MatterActivityViewModel by viewModels()

//    private lateinit var localDevicesUiModel: DevicesUiModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val matterDeviceViewModel =
            ViewModelProvider(this).get(MatterDeviceViewModel::class.java)

        lifecycle.addObserver(matterDeviceViewModel)
        _binding = FragmentMatterDeviceBinding.inflate(inflater, container, false)

        /** Devices List Recycle View **/
        val devicesListRecyclerView = binding.devicesList

        // Setup recycler view for the devices list
        devicesListRecyclerView.layoutManager = GridLayoutManager(context, 3)
        devicesListRecyclerView.adapter = MatterDevicesAdapter(listOf())

        matterDeviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            devicesListRecyclerView.adapter = MatterDevicesAdapter(devices)
        }

        /** Dynamic set UI elements **/
        val addNewDeviceButton: FloatingActionButton = binding.addDevicesBtn


        // Matter
        // Observe the devicesLiveData.
        viewModel.devicesUiModelLiveData.observe(viewLifecycleOwner) { devicesUiModel: DevicesUiModel ->
            // done: Andrew - Grab one of the devices from devicesUiModel and save in variable called deviceUiModel (done)
//            localDevicesUiModel = devicesUiModel

            if (devicesUiModel.devices.isNotEmpty()) {
                Timber.i("devicesUiModel.devices is not empty: ${devicesUiModel.devices.count()}")
//                deviceUiModel = devicesUiModel.devices[0]
//                var deviceUiModel = devicesUiModel.devices[0]
//                deviceUiModel.device.deviceId
            } else {
                Timber.i("devicesUiModel.devices is empty")
            }

            matterDeviceViewModel.updateDeviceStates(devicesUiModel.devices)
            matterDeviceViewModel.addDevice(devicesUiModel.devices)
        }
        viewModel.commissionDeviceStatus.observe(viewLifecycleOwner) { status ->
            Timber.d("commissionDeviceStatus.observe: status [${status}]")
        }
        viewModel.commissionDeviceIntentSender.observe(viewLifecycleOwner) { sender ->
            Timber.d("commissionDeviceIntentSender.observe is called with sender [${sender}]")
            if (sender != null) {
                // Commission Device Step 4: Launch the activity described in the IntentSender that
                // was returned in Step 3 where the viewModel calls the GPS API to commission
                // the device.
                Timber.d("*** Calling commissionDeviceLauncher.launch")
                commissionDeviceLauncher.launch(IntentSenderRequest.Builder(sender).build())
            }
        }

        // commission to development fabric
        commissionDeviceLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                // Commission Device Step 5.
                // The Commission Device activity in GPS has completed.
                val resultCode = result.resultCode
                Timber.d("GOT result for commissioningLauncher: resultCode [${resultCode}]")
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.commissionDeviceSucceeded(result, "Commissioning succeeded")
                } else {
                    viewModel.commissionDeviceFailed("Commissioning failed ${resultCode}")
                }
            }

        // Setup Interactions Within Devices View
        addNewDeviceButton.setOnClickListener {
            Timber.d("addDeviceButton.setOnClickListener")
            viewModel.stopDevicesPeriodicPing()
            viewModel.commissionDevice(requireActivity().intent, requireContext())
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val intent = requireActivity().intent
        if (isMultiAdminCommissioning(intent)) {
            Timber.d("*** MultiAdminCommissioning ***")
            if (viewModel.commissionDeviceStatus.value == TaskStatus.NotStarted) {
                Timber.d("TaskStatus.NotStarted so starting commissioning")
                viewModel.commissionDevice(intent, requireContext())
            } else {
                Timber.d("TaskStatus is not NotStarted: ${viewModel.commissionDeviceStatus.value}")
            }
        } else {
            Timber.d("*** Main ***")
            if (PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS != -1) {
                Timber.d("Starting periodic ping on devices")
                viewModel.startDevicesPeriodicPing()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        Timber.d("onPause(): Stopping periodic ping on devices")
        viewModel.stopDevicesPeriodicPing()
    }
}
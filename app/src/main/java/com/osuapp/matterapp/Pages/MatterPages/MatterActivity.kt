package com.osuapp.matterapp.Pages.MatterPages

import android.app.Activity
import android.os.Bundle
import timber.log.Timber
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.osuapp.matterapp.*
import com.osuapp.matterapp.MatterActivityViewModel
import com.osuapp.matterapp.databinding.ActivityMatterBinding


class MatterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatterBinding

    // The ActivityResult launcher that launches the "commissionDevice" activity in Google Play services.
    private lateinit var commissionDeviceLauncher: ActivityResultLauncher<IntentSenderRequest>

    private lateinit var viewModel: MatterActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // view model setup
        viewModel = ViewModelProvider(this).get(MatterActivityViewModel::class.java)
        // Observe the devicesLiveData.
        viewModel.devicesUiModelLiveData.observe(this) { devicesUiModel: DevicesUiModel ->
            // TODO: Andrew - Grab one of the devices from devicesUiModel and save in variable called deviceUiModel
        }
        // TODO: Zach - Uncomment code and resolve errors
//        viewModel.commissionDeviceStatus.observe(this) { status ->
//            Timber.d("commissionDeviceStatus.observe: status [${status}]")
//        }
//        viewModel.commissionDeviceIntentSender.observe(this) { sender ->
//            Timber.d("commissionDeviceIntentSender.observe is called with sender [${sender}]")
//            if (sender != null) {
//                // Commission Device Step 4: Launch the activity described in the IntentSender that
//                // was returned in Step 3 where the viewModel calls the GPS API to commission
//                // the device.
//                Timber.d("*** Calling commissionDeviceLauncher.launch")
//                commissionDeviceLauncher.launch(IntentSenderRequest.Builder(sender).build())
//            }
//        }

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

        // button on click listener
        binding.addDeviceButton.setOnClickListener {
            Timber.d("addDeviceButton.setOnClickListener")
            // TODO: Zach - Uncomment code and resolve errors
//            viewModel.stopDevicesPeriodicPing()
//            viewModel.commissionDevice(intent, this)
        }

        // switch on click listener
        binding.switch1.setOnClickListener {
            Timber.d("onOff switch onClickListener")
            Timber.d("onOff switch state: [${binding.switch1.isChecked}]")

            // TODO: Andrew - Uncomment code and resolve errors
//            viewModel.updateDeviceStateOn(deviceUiModel, binding.switch1.isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume()")

        val intent = intent
        // TODO: Zach - Uncomment code and resolve errors
//        if (isMultiAdminCommissioning(intent)) {
//            Timber.d("*** MultiAdminCommissioning ***")
//            if (viewModel.commissionDeviceStatus.value == TaskStatus.NotStarted) {
//                Timber.d("TaskStatus.NotStarted so starting commissioning")
//                viewModel.commissionDevice(intent, this)
//            } else {
//                Timber.d("TaskStatus is not NotStarted: ${viewModel.commissionDeviceStatus.value}")
//            }
//        } else {
//            Timber.d("*** Main ***")
//            if (PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS != -1) {
//                Timber.d("Starting periodic ping on devices")
//                viewModel.startDevicesPeriodicPing()
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause(): Stopping periodic ping on devices")
        // TODO: Zach - Uncomment code and resolve errors
//        viewModel.stopDevicesPeriodicPing()
    }

}

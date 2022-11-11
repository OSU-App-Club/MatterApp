package com.osuapp.matterapp

import android.app.Activity
import android.os.Bundle
import timber.log.Timber
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import dagger.hilt.android.AndroidEntryPoint
import com.osuapp.matterapp.R
import com.osuapp.matterapp.databinding.ActivityMainBinding
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import com.google.android.material.switchmaterial.SwitchMaterial
import com.osuapp.matterapp.chip.ClustersHelper
import com.osuapp.matterapp.data.DevicesRepository
import com.osuapp.matterapp.data.DevicesStateRepository
import com.osuapp.matterapp.data.UserPreferencesRepository
import kotlinx.coroutines.launch


/** Main Activity for the "Google Home Sample App for Matter" (GHSAFM). */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var downloadModuleLauncher: ActivityResultLauncher<IntentSenderRequest>

    /** Kicks off the download module intent for the Home module via [ModuleInstallClient] */
    private fun downloadModule(downloadModuleLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        ModuleInstall.getClient(this)
            .getInstallModulesIntent(Matter.getCommissioningClient(this))
            .addOnSuccessListener { response ->
                if (response.pendingIntent != null) {
                    downloadModuleLauncher.launch(
                        IntentSenderRequest.Builder(response.pendingIntent!!.intentSender).build()
                    )
                } else {
                    Timber.i("Home Module Install module already installed")
                }
            }
            .addOnFailureListener { ex ->
                Timber.e(ex,"Home Module Install download failed")
            }
    }

    /**
     * Constants we access from Utils, but that depend on the Activity context to be set to their
     * values.
     */
    fun initContextDependentConstants() {
        // versionName is set in build.gradle.
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        VERSION_NAME = packageInfo.versionName

        // Strings associated with DeviceTypes
        setDeviceTypeStrings(
            unspecified = "unspecified",
            light = "light",
            outlet = "outlet",
            unknown = "unknown")
    }

    // The ActivityResult launcher that launches the "commissionDevice" activity in Google Play services.
    private lateinit var commissionDeviceLauncher: ActivityResultLauncher<IntentSenderRequest>


    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Useful to see which preferences are set under the hood by Matter libraries.
        displayPreferences(this)

        initContextDependentConstants()

        downloadModuleLauncher =
            registerForActivityResult<IntentSenderRequest, ActivityResult>(StartIntentSenderForResult()) { result ->
                val resultCode = result.getResultCode()
                if (resultCode == RESULT_OK) {
                    Timber.i("Home Module Install download complete")
                } else if (resultCode == RESULT_CANCELED) {
                    Timber.e("Home Module Install download canceled")
                }
            }
        downloadModule(downloadModuleLauncher);

        var deviceUiModel: DeviceUiModel? = null;
        // view model setup
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        // Observe the devicesLiveData.
        viewModel.devicesUiModelLiveData.observe(this) { devicesUiModel: DevicesUiModel ->
            // done: Andrew - Grab one of the devices from devicesUiModel and save in variable called deviceUiModel (done)
            deviceUiModel = devicesUiModel.devices[0]
        }
        // done: Zach - Uncomment code and resolve errors

        viewModel.commissionDeviceStatus.observe(this) { status ->
            Timber.d("commissionDeviceStatus.observe: status [${status}]")
        }
        viewModel.commissionDeviceIntentSender.observe(this) { sender ->
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

        // button on click listener
        binding.addDeviceButton.setOnClickListener {
            Timber.d("addDeviceButton.setOnClickListener")
            // done: Zach - Uncomment code and resolve errors
            viewModel.stopDevicesPeriodicPing()
            viewModel.commissionDevice(intent, this)
        }

        // switch on click listener
        binding.switch1.setOnClickListener {
            Timber.d("onOff switch onClickListener")
            Timber.d("onOff switch state: [${binding.switch1.isChecked}]")
            //if(deviceUiModel != null){

            //}
            // done: Andrew - Uncomment code and resolve errors
            viewModel.updateDeviceStateOn(deviceUiModel!!, binding.switch1.isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume()")

        val intent = intent
        // done: Zach - Uncomment code and resolve errors
        if (isMultiAdminCommissioning(intent)) {
            Timber.d("*** MultiAdminCommissioning ***")
            if (viewModel.commissionDeviceStatus.value == TaskStatus.NotStarted) {
                Timber.d("TaskStatus.NotStarted so starting commissioning")
                viewModel.commissionDevice(intent, this)
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
        // done: Zach - Uncomment code and resolve errors
        viewModel.stopDevicesPeriodicPing()
    }

}

package com.osuapp.matterapp.Pages.MatterPages

import android.app.Activity
import android.os.Bundle
import android.util.Log
import timber.log.Timber
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.osuapp.matterapp.PERIODIC_UPDATE_INTERVAL_HOME_SCREEN_SECONDS
import com.osuapp.matterapp.TaskStatus
import com.osuapp.matterapp.databinding.ActivityMatterBinding
import com.osuapp.matterapp.isMultiAdminCommissioning
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatterBinding

    private val viewModel: MatterActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var deviceUiModel: DeviceUiModel? = null;
        // view model setup

//        viewModel = ViewModelProvider(this).get(MatterActivityViewModel::class.java)

        // done: Zach - Uncomment code and resolve errors



        // switch on click listener
        binding.switch1.setOnClickListener {
            Timber.d("onOff switch onClickListener")
            Timber.d("onOff switch state: [${binding.switch1.isChecked}]")
            if(deviceUiModel != null){
                // done: Andrew - Uncomment code and resolve errors
                viewModel.updateDeviceStateOn(deviceUiModel!!, binding.switch1.isChecked)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

}

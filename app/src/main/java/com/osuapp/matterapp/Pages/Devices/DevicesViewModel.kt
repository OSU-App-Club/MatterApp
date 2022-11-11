package com.osuapp.matterapp.Pages.Devices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DevicesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Devices Fragment"
    }

    val text: LiveData<String> = _text

}
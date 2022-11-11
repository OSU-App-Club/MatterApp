package com.osuapp.matterapp.Pages.Profile

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileActivityViewModel : ViewModel(), DefaultLifecycleObserver {

    private val _text = MutableLiveData<String>().apply {
        value = "Profile Fragment"
    }

    val text: LiveData<String> = _text

}
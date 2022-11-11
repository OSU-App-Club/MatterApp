package com.osuapp.matterapp.Pages.Schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScheduleViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Schedule Fragment"
    }

    val text: LiveData<String> = _text

}
package com.osuapp.matterapp.Pages.Groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GroupsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Groups Fragment"
    }

    val text: LiveData<String> = _text
}
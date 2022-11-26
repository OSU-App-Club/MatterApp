package com.example.navigationandmvvm.ViewsAndViewModels.devicesEditor

import androidx.lifecycle.*

class DevicesEditorViewModel : ViewModel(), DefaultLifecycleObserver {

    private val _text = MutableLiveData<String>().apply {
        value = "Devices Editor Fragment"
    }
    val text: LiveData<String> = _text

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
    }
}
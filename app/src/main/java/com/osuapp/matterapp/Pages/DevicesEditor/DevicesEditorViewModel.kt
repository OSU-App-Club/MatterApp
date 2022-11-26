package com.osuapp.matterapp.Pages.DevicesEditor

import android.util.Log
import androidx.lifecycle.*
import com.osuapp.matterapp.Pages.Devices.MatterDeviceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import shared.Models.Device
import shared.Utility.CatApi
import kotlin.reflect.jvm.internal.impl.types.AbstractTypeCheckerContext.SupertypesPolicy.None

class DevicesEditorViewModel : ViewModel(), DefaultLifecycleObserver {
    private val _TAG = MatterDeviceViewModel::class.java.simpleName
    private val matterDevicesViewModelJob = Job()
    private var coroutineScope = CoroutineScope(matterDevicesViewModelJob + Dispatchers.Main)

    private val _text = MutableLiveData<String>().apply {
        value = "Devices Editor Fragment"
    }
    val text: LiveData<String> = _text

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
    }

    fun getDevice(deviceId: String, completion: (Device?) -> Unit) {
        var device: Device? = null
        coroutineScope.launch {
            try {
                val responseJson = CatApi.retrofitService.getDeviceById(deviceId).await()
                Log.i(_TAG, "Async getDeviceById success: $responseJson")
                val deviceJson = JSONObject(responseJson).getJSONObject("device")
                device = Device(deviceJson)
                completion(device)
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(_TAG, "Async getDeviceById failed: ${e.localizedMessage}")
                completion(null)
            }
        }
    }

    fun updateDevice(device: Device) {
        coroutineScope.launch {
            try {
                val deviceId = device.getDeviceId()
                val deviceJson = device.deviceToJson()
                val body: RequestBody = RequestBody.create(MediaType.parse("application/json"), deviceJson)
                val awsHttpResponse = CatApi.retrofitService.updateDeviceById(deviceId, body).await()
                Log.e(_TAG, "Async updateDeviceById success: $awsHttpResponse")
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(_TAG, "Async updateDeviceById failed: ${e.localizedMessage}")
            }
        }
    }

    fun deleteDevice(deviceId: String) {
        coroutineScope.launch {
            try {
                val awsHttpResponse = CatApi.retrofitService.deleteDeviceById(deviceId).await()
                Log.e(_TAG, "Async deleteDeviceById success: $awsHttpResponse")
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(_TAG, "Async deleteDeviceById failed: ${e.localizedMessage}")
            }
        }
    }
}
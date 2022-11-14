package Shared.Models

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject

class Device (
    data: JSONObject
) {

    /************ Utilities ************/
    private val TAG = Device::class.java.simpleName

    /************ Properties ************/
    private lateinit var id: String
    private lateinit var name: String
    private var active: Boolean = false
    private lateinit var wifi: String
    private lateinit var deviceType: String
    private lateinit var groups: MutableList<String>

    /************ Initialize Class ************/
    init {
        try {
            // Fetch properties from the Json object to serialize and store as primitive types
            this.id = data.getString("id")
            this.name = data.getString("name")
            this.active = data.getBoolean("active")
            this.wifi = data.getString("wifi")
            this.deviceType = data.getString("deviceType")

            // Since groups is an array, fetch the list to iterate over
            val jsonGroups = data.getJSONArray("groups")

            // Instantiate the mutable list so we can append to it.
            this.groups = mutableListOf()

            // Add each group to the private Device groups
            for ( i in 0 until jsonGroups.length() ) {
                this.groups.add(jsonGroups[i].toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Device Constructor Error: $e")
        }
    }

    /************ Getters ************/

    fun getDeviceId(): String {
        return this.id
    }

    fun getDeviceName(): String {
        return this.name
    }

    fun isActive(): Boolean {
        return this.active
    }

    fun getDeviceWiFi(): String {
        return this.wifi
    }

    fun getDeviceType(): String {
        return this.deviceType
    }

    fun getDeviceGroups(): MutableList<String> {
        return this.groups
    }

    fun getDeviceGroupIndex(index: Int): String {
        return this.groups[index]
    }

    /************ Setters ************/

    fun setDeviceName(renameTo: String) {
        this.name = renameTo
    }

    fun setActive(active: Boolean) {
        this.active = active
    }

    fun setDeviceWiFi(WiFiName: String) {
        this.wifi = WiFiName
    }

    fun setDeviceType(type: String) {
        this.deviceType = type
    }

    fun setDeviceGroups(newGroups: Array<String>) {
        this.groups = newGroups.toMutableList()
    }

    /************ Public Methods ************/
    fun deviceToJson(): String? {
        return Gson().toJson(this)
    }
}
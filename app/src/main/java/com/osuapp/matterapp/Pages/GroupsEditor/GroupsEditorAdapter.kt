package com.osuapp.matterapp.Pages.GroupsEditor

import android.bluetooth.BluetoothClass.Device
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.osuapp.matterapp.R

class GroupsEditorAdapter(var devicesInGroup : List<GroupsEditorViewModel.DevicesListItem>,
                          var onDeviceRemoved: ((deviceId: String) -> Unit)
)
    : RecyclerView.Adapter<GroupsEditorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView
        val removeBtn : Button

        init {
            this.name = itemView.findViewById(R.id.nameTxt)
            this.removeBtn = itemView.findViewById(R.id.removeBtn)
        }
    }

    /** Adapter overriden functions **/
    override fun getItemCount(): Int {
        // Return the number of devices in the dynamic devices list
        return devicesInGroup.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_edit_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devicesInGroup[position]

        holder.name.text = device.name
        holder.removeBtn.setOnClickListener {
            try {
//                Toast.makeText(holder.itemView.context, "Remove ${device.name}", Toast.LENGTH_SHORT)
//                    .show()
                val deviceId = devicesInGroup[position].id
                onDeviceRemoved(deviceId)
            } catch (e: Exception) {
//                Toast.makeText(holder.itemView.context, "Error removing device", Toast.LENGTH_SHORT)
//                    .show()
            }
        }
    }
}
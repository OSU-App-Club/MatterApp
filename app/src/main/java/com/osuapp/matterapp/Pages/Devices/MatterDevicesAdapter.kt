package com.osuapp.matterapp.Pages.Devices

import com.osuapp.matterapp.Pages.DevicesEditor.DevicesEditorActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.osuapp.matterapp.R

class MatterDevicesAdapter(var devices : List<MatterDeviceViewModel.DevicesListItem>)
    : RecyclerView.Adapter<MatterDevicesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label : TextView
        val image : ImageView

        init {
            this.label = itemView.findViewById(R.id.device_label)
            this.image = itemView.findViewById(R.id.device_image)
        }
    }

    /** Adapter overriden functions **/
    override fun getItemCount(): Int {
        // Return the number of devices in the dynamic devices list
        return devices.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.devices_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]

        holder.label.text = device.label
        holder.image.setImageResource(R.drawable.ic_std_device)

        // Setup onClick interaction to Devices Editor page
        holder.itemView.setOnClickListener {
            // TODO: Setup specific devices editor page
            // launch DevicesEditor activity
            val intent = Intent(holder.itemView.context, DevicesEditorActivity::class.java)
            holder.itemView.context.startActivity(intent)
//            holder.itemView.findNavController().navigate(R.id.action_navigation_devices_to_devicesEditorFragment)
        }
    }
}
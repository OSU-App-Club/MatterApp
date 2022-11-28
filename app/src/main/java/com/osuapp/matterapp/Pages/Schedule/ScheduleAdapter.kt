package com.osuapp.matterapp.Pages.Schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.osuapp.matterapp.R

class ScheduleAdapter(var schedules : List<ScheduleViewModel.ScheduleListItem>)
    : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time : TextView
        val enabled : Switch

        init {
            this.time = itemView.findViewById(R.id.nameTxt)
            this.enabled = itemView.findViewById(R.id.enabledSw)
        }
    }

    /** Adapter overriden functions **/
    override fun getItemCount(): Int {
        // Return the number of devices in the dynamic devices list
        return schedules.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = schedules[position]

        holder.time.text = schedule.time
        holder.enabled.isChecked = schedule.enabled

        // Setup onClick interaction to Devices Editor page
        holder.itemView.setOnClickListener {
            // launch DevicesEditor activity
//            val intent = Intent(holder.itemView.context, DevicesEditorActivity::class.java)
//            intent.putExtra("deviceId", device.id)
//            holder.itemView.context.startActivity(intent)
        }
    }
}
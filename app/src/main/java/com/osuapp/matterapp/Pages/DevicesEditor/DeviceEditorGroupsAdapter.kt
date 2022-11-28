package com.osuapp.matterapp.Pages.DevicesEditor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.osuapp.matterapp.R

class DeviceEditorGroupsAdapter(var groups : List<String>)
    : RecyclerView.Adapter<DeviceEditorGroupsAdapter.ViewHolder>() {

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
        return groups.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_edit_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]

        holder.name.text = group
        holder.removeBtn.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Remove $group", Toast.LENGTH_SHORT).show()
        }
    }
}
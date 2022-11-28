package com.osuapp.matterapp.Pages.GroupsEditorPart2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.osuapp.matterapp.R
import timber.log.Timber

class GroupsEditorAdapterPart2(var devicesNotInGroup : List<GroupsEditorViewModelPart2.DevicesListItem>)
    : RecyclerView.Adapter<GroupsEditorAdapterPart2.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var index : Int = 0
        val name : TextView
        val checkbox : CheckBox

        init {
            this.name = itemView.findViewById(R.id.nameTxt)
            this.checkbox = itemView.findViewById(R.id.checkBox2)
        }
    }

    /** Adapter overriden functions **/
    override fun getItemCount(): Int {
        // Return the number of devices in the dynamic devices list
        return devicesNotInGroup.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_edit_list_item_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devicesNotInGroup[position]

        holder.index = position //Does this hold up to scrutiny?
        holder.name.text = device.name
        holder.itemView.setOnClickListener {
            Timber.i("Clicked on device: ${device.name}")
            holder.checkbox.isChecked = !holder.checkbox.isChecked

            val deviceIndex = holder.index
            val selected = holder.checkbox.isChecked
            Timber.i("deviceIndex: $deviceIndex, selected: $selected")
            // Update device's attribute "include" bool
            devicesNotInGroup[deviceIndex].include = selected
        }

        // When checkbox clicked
        holder.checkbox.setOnClickListener() {
            val deviceIndex = holder.index
            val selected = holder.checkbox.isChecked
            Timber.i("deviceIndex: $deviceIndex, selected: $selected")
            // Update device's attribute "include" bool
            devicesNotInGroup[deviceIndex].include = selected
        }
    }
}
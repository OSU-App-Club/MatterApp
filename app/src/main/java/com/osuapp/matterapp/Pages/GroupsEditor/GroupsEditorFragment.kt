package com.osuapp.matterapp.Pages.GroupsEditor

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.osuapp.matterapp.databinding.FragmentGroupsEditorBinding

class GroupsEditorFragment : AppCompatActivity() {

    private lateinit var _binding: FragmentGroupsEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = FragmentGroupsEditorBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        val groupsEditorViewModel = ViewModelProvider(this).get(GroupsEditorViewModel::class.java)
        lifecycle.addObserver(groupsEditorViewModel)

        val textView: TextView = _binding.groupsEditorTextHeader

        groupsEditorViewModel.text.observe(this) {
            textView.text = it
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
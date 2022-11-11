package com.osuapp.matterapp.Pages.Profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.osuapp.matterapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        val textView: TextView = _binding.profileTitleText

        val matterProfileActivityViewModel = ViewModelProvider(this).get(ProfileActivityViewModel::class.java)
        lifecycle.addObserver(matterProfileActivityViewModel)

        matterProfileActivityViewModel.text.observe(this) {
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
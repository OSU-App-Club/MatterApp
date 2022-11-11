package com.osuapp.matterapp.Pages.Page1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.osuapp.matterapp.Pages.Page1.Page1ViewModel
import com.osuapp.matterapp.R
import com.osuapp.matterapp.databinding.ActivityMainBinding
import com.osuapp.matterapp.databinding.ActivityPage1Binding

class Page1View : AppCompatActivity() {
    private val TAG = Page1View::class.java.simpleName
    private lateinit var binding: ActivityPage1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_page1)

        Log.i(TAG, "onCreate from Page1View")

        // create binding
        binding = ActivityPage1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val text: TextView = binding.countTxt
        val incrementBtn = binding.incrementBtn
        val goBackBtn = binding.goBackBtn

        val viewModel = ViewModelProvider(this).get(Page1ViewModel::class.java)
        lifecycle.addObserver(viewModel)
        viewModel.count.observe(this) {
            text.text = "Count: ${it}"
        }

        incrementBtn.setOnClickListener {
            viewModel.increment()
        }
        goBackBtn.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume from Page1View")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause from Page1View")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy from Page1View")
    }
}
package com.osuapp.matterapp.Pages.Schedule

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.osuapp.matterapp.databinding.FragmentScheduleBinding

class ScheduleFragment : Fragment() {

    private lateinit var _binding: FragmentScheduleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val matterScheduleViewModel =
            ViewModelProvider(this).get(ScheduleViewModel::class.java)
        lifecycle.addObserver(matterScheduleViewModel)

        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        val root: View = _binding.root

        /** Groups List Recycle View **/
        val scheduleList = _binding.scheduleList

        // Setup recycler view for the devices list
        scheduleList.layoutManager = GridLayoutManager(context, 1)
        scheduleList.adapter = ScheduleAdapter(listOf())

        matterScheduleViewModel.schedules.observe(viewLifecycleOwner) {
            scheduleList.adapter = ScheduleAdapter(it)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
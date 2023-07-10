package com.lrm.birthdayreminder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lrm.birthdayreminder.BirthdayApplication
import com.lrm.birthdayreminder.databinding.FragmentAddEventBinding
import com.lrm.birthdayreminder.viewModel.EventViewModel
import com.lrm.birthdayreminder.viewModel.EventViewModelFactory

class AddEventFragment : Fragment() {

    private val viewModel: EventViewModel by activityViewModels {
        EventViewModelFactory(
            (activity?.application as BirthdayApplication).database.eventDao()
        )
    }

    private var _binding: FragmentAddEventBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backIcon.setOnClickListener {
            this.findNavController().navigateUp()
        }

        binding.cancel.setOnClickListener {
            val action =
                AddEventFragmentDirections.actionAddEventFragmentToBirthdaysListFragment()
            this.findNavController().navigate(action)
        }

        binding.save.setOnClickListener {
            addNewEvent()
        }
    }

    private fun addNewEvent() {

        val name = binding.name.text.toString()
        var eventType = ""
        val day = binding.day.text.toString()
        val month = binding.month.text.toString()
        val year = binding.year.text.toString()

        if (binding.birthdayEvent.isChecked) {
            eventType ="0"
        } else if (binding.anniversaryEvent.isChecked) {
            eventType = "1"
        }

        Log.i("AddEventFragment", "addNewEvent: $name , $eventType, $day, $month, $year")

        if (viewModel.isEntryValid(name, day, month, year)) {

            val age = viewModel.calculateAge(month.toInt(), year.toInt())

            viewModel.addNewEvent(
                name,
                eventType.toInt(),
                day.toInt(),
                month.toInt(),
                year.toInt(), age)

            val action =
                AddEventFragmentDirections.actionAddEventFragmentToBirthdaysListFragment()
            this.findNavController().navigate(action)
        } else {
            Toast.makeText(requireContext(), "Please enter correct data...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
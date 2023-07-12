package com.lrm.birthdayreminder.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lrm.birthdayreminder.BirthdayApplication
import com.lrm.birthdayreminder.R
import com.lrm.birthdayreminder.database.Event
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

    private val navigationArgs: AddEventFragmentArgs by navArgs()
    lateinit var event: Event

    var dobDay: String = ""
    var dobMonth: String = ""
    var dobYear: String = ""


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

        val id = navigationArgs.eventId

        if (id > 0) {
            viewModel.retrieveEvent(id).observe(this.viewLifecycleOwner) {selectedEvent ->
                event = selectedEvent
                bind(event)
            }
            binding.fragmentLabel.text = "Edit Event"
        } else {
            binding.fragmentLabel.text = "Add Event"
            binding.save.setOnClickListener {
                addNewEvent()
            }
        }


        val datePickerDialog = DatePickerDialog(requireContext(),
            DatePickerDialog.OnDateSetListener { _, dob_year, dob_month, dob_day ->

                dobDay = dob_day.toString()
                dobMonth = (dob_month + 1).toString()
                dobYear = dob_year.toString()
                binding.dobDate.text = "$dobDay/$dobMonth/$dobYear"
        }
            , viewModel.currentYear, viewModel.currentMonth, viewModel.currentDay)

        binding.datePicker.setOnClickListener {
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.birthdayEvent.setOnClickListener {
            if (binding.birthdayEvent.isChecked) {
                binding.cakeImg.setImageResource(R.drawable.app_icon)
            }
        }

        binding.anniversaryEvent.setOnClickListener {
            if (binding.anniversaryEvent.isChecked) {
                binding.cakeImg.setImageResource(R.drawable.cake_icon)
            }
        }
    }

    private fun bind(event: Event) {
        binding.apply {
            name.setText(event.name, TextView.BufferType.SPANNABLE)

            dobDay = event.day.toString()
            dobMonth = event.month.toString()
            dobYear = event.year.toString()

            dobDate.text = "$dobDay/$dobMonth/$dobYear"

            if (event.eventType == 0) {
                binding.birthdayEvent.isChecked = true
                binding.anniversaryEvent.isChecked = false
                binding.cakeImg.setImageResource(R.drawable.app_icon)
            } else if (event.eventType == 1) {
                binding.anniversaryEvent.isChecked = true
                binding.birthdayEvent.isChecked = false
                binding.cakeImg.setImageResource(R.drawable.cake_icon)
            }

            save.setOnClickListener { updateEvent() }
        }
    }

    private fun addNewEvent() {

        val name = binding.name.text.toString()
        var eventType = ""

        if (binding.birthdayEvent.isChecked) {
            eventType ="0"
        } else if (binding.anniversaryEvent.isChecked) {
            eventType = "1"
        }

        Log.i("AddEventFragment", "addNewEvent: $name , $eventType, $dobDay, $dobMonth, $dobYear")

        if (viewModel.isEntryValid(name, dobDay, dobMonth, dobYear)) {

            viewModel.addNewEvent(
                name,
                eventType.toInt(),
                dobDay.toInt(),
                dobMonth.toInt(),
                dobYear.toInt())

            val action =
                AddEventFragmentDirections.actionAddEventFragmentToBirthdaysListFragment()
            this.findNavController().navigate(action)
        } else {
            Toast.makeText(requireContext(), "Please enter correct data...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEvent() {

        val name = binding.name.text.toString()
        var eventType = ""

        if (binding.birthdayEvent.isChecked) {
            eventType ="0"
        } else if (binding.anniversaryEvent.isChecked) {
            eventType = "1"
        }

        if (viewModel.isEntryValid(name, dobDay, dobMonth, dobYear)) {
            viewModel.updateEvent(
                this.navigationArgs.eventId,
                name, eventType.toInt(),
                dobDay.toInt(),
                dobMonth.toInt(),
                dobYear.toInt()
            )

            val action = AddEventFragmentDirections.actionAddEventFragmentToBirthdaysListFragment()
            this.findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
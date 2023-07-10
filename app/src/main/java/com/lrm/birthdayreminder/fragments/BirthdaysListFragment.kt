package com.lrm.birthdayreminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lrm.birthdayreminder.BirthdayApplication
import com.lrm.birthdayreminder.adapter.EventListAdapter
import com.lrm.birthdayreminder.databinding.FragmentBirthdaysListBinding
import com.lrm.birthdayreminder.viewModel.EventViewModel
import com.lrm.birthdayreminder.viewModel.EventViewModelFactory

class BirthdaysListFragment : Fragment() {

    private val viewModel: EventViewModel by activityViewModels {
        EventViewModelFactory(
            (activity?.application as BirthdayApplication).database.eventDao()
        )
    }

    private var _binding: FragmentBirthdaysListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirthdaysListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settings.setOnClickListener {
            val action = BirthdaysListFragmentDirections.actionBirthdaysListFragmentToSettingsFragment()
            this.findNavController().navigate(action)
        }

        binding.addEventFab.setOnClickListener {
            val action = BirthdaysListFragmentDirections.actionBirthdaysListFragmentToAddEventFragment()
            this.findNavController().navigate(action)
        }

        val adapter = EventListAdapter{
            val action =
                BirthdaysListFragmentDirections.actionBirthdaysListFragmentToEventDetailFragment(it.id)
            this.findNavController().navigate(action)
        }

        binding.recyclerView.adapter = adapter
        viewModel.allEvents.observe(this.viewLifecycleOwner) {events ->
            events.let { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
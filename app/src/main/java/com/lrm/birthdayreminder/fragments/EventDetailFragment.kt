package com.lrm.birthdayreminder.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lrm.birthdayreminder.BirthdayApplication
import com.lrm.birthdayreminder.R
import com.lrm.birthdayreminder.database.Event
import com.lrm.birthdayreminder.databinding.FragmentEventDetailBinding
import com.lrm.birthdayreminder.viewModel.EventViewModel
import com.lrm.birthdayreminder.viewModel.EventViewModelFactory

class EventDetailFragment : Fragment() {

    private val viewModel: EventViewModel by activityViewModels {
        EventViewModelFactory(
            (activity?.application as BirthdayApplication).database.eventDao()
        )
    }

    private val navigationArgs: EventDetailFragmentArgs by navArgs()
    private lateinit var event: Event

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.GONE
        binding.gridLayout.visibility = View.GONE
        binding.fullAgeResultLL.visibility = View.GONE

        val id = navigationArgs.eventId
        viewModel.retrieveEvent(id).observe(this.viewLifecycleOwner) {selectedEvent ->
            event = selectedEvent
            bind(event)
        }

        binding.getAgeButton.setOnClickListener {
            getFullAge()
        }
    }

    private fun bind(event: Event) {
        binding.apply {
            name.text = event.name
            eventDate.text = "${event.day}/${event.month}/${event.year}"
            age.text = "${event.ageInYears}"
            deleteButton.setOnClickListener { showConfirmationDialog() }
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) {_, _ -> }
            .setPositiveButton(getString(R.string.yes)) {_, _ ->
                deleteItem(event)
            }
            .show()
    }

    private fun deleteItem(event: Event) {
        viewModel.deleteEvent(event)
        this.findNavController().navigateUp()
    }

    private fun getFullAge() {
        binding.progressBar.visibility = View.VISIBLE
        binding.gridLayout.visibility = View.VISIBLE
        binding.fullAgeResultLL.visibility = View.VISIBLE
        ObjectAnimator.ofInt(binding.progressBar, "progress",100)
            .setDuration(3000)
            .start()

        viewModel.calculateFullAge(
            event.day, event.month, event.year
        )

        binding.fullAgeResult.text = ""

        binding.shimmer1.visibility = View.VISIBLE
        binding.shimmer2.visibility = View.VISIBLE
        binding.shimmer3.visibility = View.VISIBLE
        binding.shimmer4.visibility = View.VISIBLE
        binding.shimmer5.visibility = View.VISIBLE
        binding.shimmer6.visibility = View.VISIBLE
        binding.shimmer7.visibility = View.VISIBLE
        binding.shimmerFullAge.visibility = View.VISIBLE

        binding.fullAgeResult.visibility = View.GONE
        binding.yearsResult.visibility = View.GONE
        binding.monthsResult.visibility = View.GONE
        binding.weeksResult.visibility = View.GONE
        binding.daysResult.visibility = View.GONE
        binding.hoursResult.visibility = View.GONE
        binding.minutesResult.visibility = View.GONE
        binding.secondsResult.visibility = View.GONE

        binding.shimmer1.startShimmer()
        binding.shimmer2.startShimmer()
        binding.shimmer3.startShimmer()
        binding.shimmer4.startShimmer()
        binding.shimmer5.startShimmer()
        binding.shimmer6.startShimmer()
        binding.shimmer7.startShimmer()
        binding.shimmerFullAge.startShimmer()

        Handler(Looper.myLooper()!!).postDelayed({
            binding.fullAgeResult.text = resources.getString(R.string.my_age_is, viewModel.fullAge)
            binding.yearsResult.text = resources.getString(R.string.years_result, viewModel.differenceOnlyInYears.toString())
            binding.monthsResult.text = resources.getString(R.string.months_result, viewModel.differenceOnlyInMonths.toString())
            binding.weeksResult.text = resources.getString(R.string.weeks_result, viewModel.differenceOnlyInWeeks.toString())
            binding.daysResult.text = resources.getString(R.string.days_result, viewModel.differenceOnlyInDays.toString())
            binding.hoursResult.text = resources.getString(R.string.hours_result, viewModel.differenceInHours.toString())
            binding.minutesResult.text = resources.getString(R.string.minutes_result, viewModel.differenceInMinutes.toString())
            binding.secondsResult.text = resources.getString(R.string.seconds_result, viewModel.differenceInSeconds.toString())

            binding.progressBar.visibility = View.INVISIBLE
            binding.progressBar.progress = 0

            binding.shimmer1.stopShimmer()
            binding.shimmer2.stopShimmer()
            binding.shimmer3.stopShimmer()
            binding.shimmer4.stopShimmer()
            binding.shimmer5.stopShimmer()
            binding.shimmer6.stopShimmer()
            binding.shimmer7.stopShimmer()
            binding.shimmerFullAge.stopShimmer()

            binding.shimmer1.visibility = View.GONE
            binding.shimmer2.visibility = View.GONE
            binding.shimmer3.visibility = View.GONE
            binding.shimmer4.visibility = View.GONE
            binding.shimmer5.visibility = View.GONE
            binding.shimmer6.visibility = View.GONE
            binding.shimmer7.visibility = View.GONE
            binding.shimmerFullAge.visibility = View.GONE

            binding.fullAgeResult.visibility = View.VISIBLE
            binding.yearsResult.visibility = View.VISIBLE
            binding.monthsResult.visibility = View.VISIBLE
            binding.weeksResult.visibility = View.VISIBLE
            binding.daysResult.visibility = View.VISIBLE
            binding.hoursResult.visibility = View.VISIBLE
            binding.minutesResult.visibility = View.VISIBLE
            binding.secondsResult.visibility = View.VISIBLE

        }, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
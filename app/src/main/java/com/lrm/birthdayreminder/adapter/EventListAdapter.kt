package com.lrm.birthdayreminder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lrm.birthdayreminder.database.Event
import com.lrm.birthdayreminder.databinding.ListItemBinding
import com.lrm.birthdayreminder.others.calculateAge
import java.util.Calendar

class EventListAdapter(private val onItemClicked: (Event) -> Unit) :
    ListAdapter<Event, EventListAdapter.EventViewHolder>(DiffCallback) {

    class EventViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(event: Event) {

            val ageInYears = calculateAge(event.day, event.month, event.year)

            binding.apply {
                name.text = event.name
                date.text = "${event.day}/${event.month}/${event.year}"
                years.text = ageInYears.toString()
            }

        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.name == newItem.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return EventViewHolder(
            ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val currentEvent = getItem(position)
        holder.bind(currentEvent)

        holder.itemView.setOnClickListener { onItemClicked(currentEvent) }
    }
}
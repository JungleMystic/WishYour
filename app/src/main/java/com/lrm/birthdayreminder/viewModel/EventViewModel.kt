package com.lrm.birthdayreminder.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lrm.birthdayreminder.constants.EVENT_REMINDER_WORK
import com.lrm.birthdayreminder.database.Event
import com.lrm.birthdayreminder.database.EventDao
import com.lrm.birthdayreminder.worker.EventReminderWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class EventViewModel(private val eventDao: EventDao) : ViewModel() {

    val allEvents = eventDao.getAllEvents().asLiveData()

    private val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    var fullAge = ""

    var differenceOnlyInYears = 0
    var differenceOnlyInMonths = 0
    var differenceOnlyInWeeks: Long = 0
    var differenceOnlyInDays: Long = 0
    var differenceInHours: Long = 0
    var differenceInMinutes: Long = 0
    var differenceInSeconds: Long = 0

    private fun insertEvent(event: Event) {
        viewModelScope.launch {
            eventDao.insert(event)
        }
    }

    private fun getEventData(
        name: String,
        eventType: Int,
        day: Int,
        month: Int,
        year: Int,
    ): Event {
        return Event(
            name = name,
            eventType = eventType,
            day = day,
            month = month,
            year = year
        )
    }

    fun addNewEvent(
        name: String,
        eventType: Int,
        day: Int,
        month: Int,
        year: Int,
    ) {
        val event = getEventData(name, eventType, day, month, year)
        insertEvent(event)
    }

    fun isEntryValid(
        name: String,
        day: String,
        month: String,
        year: String
    ): Boolean {
        if (name.isNotEmpty() && day.isNotEmpty() && month.isNotEmpty() && year.isNotEmpty()) {
            if (day.toInt() in 1..31) {
                if (month.toInt() in 1..12) {
                    if (year.toInt() in 1900..currentYear) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun calculateFullAge(dob_date: Int, dob_month: Int, dob_year: Int) {

        val mCurrentDate = currentDay
        var mCurrentMonth = currentMonth + 1
        var mCurrentYear = currentYear

        val currentDate = "$mCurrentDate/$mCurrentMonth/$mCurrentYear"
        val eventDate = "$dob_date/$dob_month/$dob_year"

        var differenceInYears: Int = 0
        var differenceInMonth: Int = 0
        var differenceInDate: Int = 0

        if (dob_date > mCurrentDate) {
            differenceInDate = (mCurrentDate + 30) - dob_date
            mCurrentMonth -= 1
        } else {
            differenceInDate = mCurrentDate - dob_date
        }

        if (dob_month > mCurrentMonth) {
            differenceInMonth = (mCurrentMonth + 12) - dob_month
            mCurrentYear -= 1
        } else {
            differenceInMonth = (mCurrentMonth - dob_month)
        }

        differenceInYears = (mCurrentYear - dob_year)


        fullAge = "$differenceInYears years, $differenceInMonth months, $differenceInDate days"


        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cD = sdf.parse(currentDate)
        val eD = sdf.parse(eventDate)

        differenceOnlyInYears = differenceInYears
        differenceOnlyInMonths = (differenceOnlyInYears * 12) + (differenceInMonth)
        differenceOnlyInWeeks = ((cD.time) - (eD.time)) / (1000 * 60 * 60 * 24 * 7)
        differenceOnlyInDays = ((cD.time) - (eD.time)) / (1000 * 60 * 60 * 24)
        differenceInHours = ((cD.time) - (eD.time)) / (1000 * 60 * 60)
        differenceInMinutes = ((cD.time) - (eD.time)) / (1000 * 60)
        differenceInSeconds = ((cD.time) - (eD.time)) / (1000)

    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventDao.delete(event)
        }
    }

    fun retrieveEvent(id: Int): LiveData<Event> {
        return eventDao.getEvent(id).asLiveData()
    }

    var scheduleTime: Long = 0

    fun setTime(time: Long) {
        scheduleTime = time
    }

    fun scheduleNotification() {

        val calendarTime = Calendar.getInstance().time
        val hours = calendarTime.hours
        val minutes = calendarTime.minutes

        val calendarTimeC = Calendar.getInstance()
        calendarTimeC.apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
        }

        val currentTime = calendarTimeC.timeInMillis

        var delayInMilliSecs = scheduleTime - currentTime

        if (delayInMilliSecs < -500) {
            delayInMilliSecs += (24 * 60 * 60 * 1000)
        }

        Log.i("EventViewModel", "scheduleNotification: $delayInMilliSecs")

        val notification = PeriodicWorkRequestBuilder<EventReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(delayInMilliSecs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            EVENT_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            notification
        )
    }

    fun cancelWork() {
        WorkManager.getInstance().cancelUniqueWork(EVENT_REMINDER_WORK)
    }

    private fun getUpdatedEventData(
        eventId: Int,
        name: String,
        eventType: Int,
        day: Int,
        month: Int,
        year: Int,
    ): Event {
        return Event(
            id = eventId,
            name = name,
            eventType = eventType,
            day = day,
            month = month,
            year = year
        )
    }

    fun updateEvent(
        eventId: Int,
        name: String,
        eventType: Int,
        day: Int,
        month: Int,
        year: Int,
    ) {
        val updatedEvent = getUpdatedEventData(eventId, name, eventType, day, month, year)
        updateEventData(updatedEvent)
    }

    private fun updateEventData(event: Event) {
        viewModelScope.launch {
            eventDao.update(event)
        }
    }
}

class EventViewModelFactory(private val eventDao: EventDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(eventDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
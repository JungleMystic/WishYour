package com.lrm.birthdayreminder.others

import java.util.Calendar

fun calculateAge(dob_day: Int, dob_month: Int, dob_year: Int): Int {
    val calendar: Calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYear = calendar.get(Calendar.YEAR)

    val mCurrentDay = currentDay
    var mCurrentMonth = currentMonth
    var mCurrentYear = currentYear

    if (dob_day > mCurrentDay) {
        mCurrentMonth -= 1
    }

    if (dob_month > mCurrentMonth) {
        mCurrentYear -= 1
    }

    return mCurrentYear - dob_year
}
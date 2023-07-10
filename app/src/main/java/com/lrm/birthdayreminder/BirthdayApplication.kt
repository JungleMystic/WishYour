package com.lrm.birthdayreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.lrm.birthdayreminder.constants.CHANNEL_DESCRIPTION
import com.lrm.birthdayreminder.constants.CHANNEL_ID
import com.lrm.birthdayreminder.constants.CHANNEL_NAME
import com.lrm.birthdayreminder.database.EventRoomDatabase

class BirthdayApplication: Application() {
    val database: EventRoomDatabase by lazy {
        EventRoomDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
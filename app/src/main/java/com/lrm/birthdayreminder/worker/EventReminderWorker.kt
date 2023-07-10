package com.lrm.birthdayreminder.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lrm.birthdayreminder.MainActivity
import com.lrm.birthdayreminder.R
import com.lrm.birthdayreminder.constants.CHANNEL_ID
import com.lrm.birthdayreminder.constants.NOTIFICATION_ID
import com.lrm.birthdayreminder.database.EventRoomDatabase
import java.util.Calendar

class EventReminderWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {

    val context = ctx

    override suspend fun doWork(): Result {

        val calendar = Calendar.getInstance()

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1

        Log.i("EventViewModel", "$day $month")

        val eventsList =
            EventRoomDatabase.getDatabase(context).eventDao().getTodayEvents(day, month)

        Log.i("EventViewModel", "$eventsList")

        if (eventsList.isNotEmpty()) {
            var message: MutableList<String> = mutableListOf()

            for (event in eventsList) {
                if (event.eventType == 0){
                    message.add("Today is the birthday of ${event.name}")
                } else if (event.eventType == 1){
                    message.add("Today is the wedding day of ${event.name}")
                }
            }

            val fullMessage = message.joinToString("\n")
            message = mutableListOf()

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent: PendingIntent = PendingIntent
                .getActivity(applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)

            val title = "Event Reminder"

            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(fullMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)


            with(NotificationManagerCompat.from(applicationContext)) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@with
                }
                notify(NOTIFICATION_ID, builder.build())
            }

            return Result.success()
        } else return Result.failure()
    }
}
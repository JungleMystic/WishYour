package com.lrm.birthdayreminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val NOTIFICATION_PREFERENCE = "notification_preference"

val Context.dataStore
        : DataStore<Preferences> by preferencesDataStore(name = NOTIFICATION_PREFERENCE)

class SettingsDataStore(context: Context) {

    private val NOTIFICATION_SWITCH = booleanPreferencesKey("notification_switch")
    private val SET_NOTIFICATION_TIME = stringPreferencesKey("set_notification_time")

    suspend fun saveNotificationSwitchMode(notificationSwitch: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_SWITCH] = notificationSwitch
        }
    }

    suspend fun saveNotificationTime(setNotificationTime: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[SET_NOTIFICATION_TIME] = setNotificationTime
        }
    }

    val preferenceFlow: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[NOTIFICATION_SWITCH] ?: false
        }

    val preferenceFlow2: Flow<String> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SET_NOTIFICATION_TIME] ?: ""
        }
}
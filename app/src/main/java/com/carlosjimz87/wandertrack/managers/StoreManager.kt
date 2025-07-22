package com.carlosjimz87.wandertrack.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.carlosjimz87.wandertrack.common.Constants.LAST_SCREEN_KEY
import com.carlosjimz87.wandertrack.common.Constants.USER_PREFS_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFS_KEY)

class StoreManager(private val context: Context) {

    companion object {
        private val LAST_SCREEN = stringPreferencesKey(LAST_SCREEN_KEY)
    }

    suspend fun saveLastScreen(screen: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SCREEN] = screen
        }
    }

    val lastScreen: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_SCREEN] ?: "Splash"
    }
}
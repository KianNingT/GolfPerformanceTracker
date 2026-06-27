package com.play.golf.perf.tracker.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "golf_theme_prefs")

@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    val isDarkTheme: Flow<Boolean> = context.themeDataStore.data
        .map { preferences ->
            preferences[Keys.IS_DARK_THEME] ?: false
        }

    suspend fun setDarkTheme(isDark: Boolean) {
        try {
            context.themeDataStore.edit { preferences ->
                preferences[Keys.IS_DARK_THEME] = isDark
            }
            Timber.d("ThemeDataStore: dark theme set to $isDark")
        } catch (e: Exception) {
            Timber.e(e, "ThemeDataStore: failed to persist theme preference")
        }
    }
}
package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_DECIMAL_PRECISION = intPreferencesKey("decimal_precision")
        val KEY_NUMBER_FORMAT = stringPreferencesKey("number_format")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DEFAULT_BASE = stringPreferencesKey("default_base_currency")
        val KEY_TARGET_CURRENCY = stringPreferencesKey("target_currency")
        val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        val KEY_LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        val KEY_AUTO_REFRESH_HOURS = intPreferencesKey("auto_refresh_hours")
    }

    val decimalPrecision: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DECIMAL_PRECISION] ?: 2
    }

    val numberFormat: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_NUMBER_FORMAT] ?: "STANDARD" // STANDARD (1,234.56) or EUROPEAN (1.234,56)
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_ENABLED] ?: true
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "DARK" // DARK by default per requirement
    }

    val defaultBaseCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_BASE] ?: "USD"
    }

    val targetCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TARGET_CURRENCY] ?: "EUR"
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PREMIUM] ?: false
    }

    val lastUpdateTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_UPDATE_TIME] ?: 0L
    }

    suspend fun setDecimalPrecision(precision: Int) {
        context.dataStore.edit { it[KEY_DECIMAL_PRECISION] = precision }
    }

    suspend fun setNumberFormat(format: String) {
        context.dataStore.edit { it[KEY_NUMBER_FORMAT] = format }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setDefaultBaseCurrency(code: String) {
        context.dataStore.edit { it[KEY_DEFAULT_BASE] = code }
    }

    suspend fun setTargetCurrency(code: String) {
        context.dataStore.edit { it[KEY_TARGET_CURRENCY] = code }
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { it[KEY_IS_PREMIUM] = premium }
    }

    suspend fun setLastUpdateTime(timeMillis: Long) {
        context.dataStore.edit { it[KEY_LAST_UPDATE_TIME] = timeMillis }
    }
}

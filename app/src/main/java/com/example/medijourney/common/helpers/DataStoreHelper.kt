package com.example.medijourney.common.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.medijourney.common.constants.Constants
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map


object DataStoreHelper {

    // Properties
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "medi_journey_data_store")

    // Functions
    suspend fun checkStringInList(context: Context, key: Preferences.Key<String>, value: String): Boolean {
        val currentList = getStringList(context, key)
        return currentList.contains(value)
    }

    suspend fun saveString(context: Context, key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun saveStringInList(context: Context, key: Preferences.Key<String>, value: String) {
        val currentList = getStringList(context, key)
        currentList.add(value)
        val json = Gson().toJson(currentList)
        context.dataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    suspend fun getString(context: Context, key: Preferences.Key<String>): String? {
        return context.dataStore.data.map { preferences ->
            preferences[key]
        }.firstOrNull()
    }

    suspend fun getString2(key: Preferences.Key<String>): String? {
        return MediJourney.getAppContext().dataStore.data.map { preferences ->
            preferences[key]
        }.firstOrNull()
    }

    suspend fun getStringList(context: Context, key: Preferences.Key<String>): MutableList<String> {
        val jsonFlow = context.dataStore.data.map { preferences ->
            preferences[key]
        }

        val json = jsonFlow.firstOrNull() ?: return mutableListOf()
        return Gson().fromJson(json, Array<String>::class.java).toMutableList()
    }

    suspend fun removeStringInList(context: Context, key: Preferences.Key<String>, value: String) {
        val currentList = getStringList(context, key)
        currentList.remove(value)
        val json = Gson().toJson(currentList)
        context.dataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    suspend fun deleteAuthenticatedPreferencesKey(context: Context) {
        context.dataStore.updateData { preferences ->
            val mutablePreferences = preferences.toMutablePreferences()
            Constants.authenticatedPreferencesKey.forEach {
                mutablePreferences.remove(it)
            }
            mutablePreferences
        }
    }

    suspend fun deleteAll(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
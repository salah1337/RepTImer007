package com.example.reptimer007.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferenceManager"

enum class sort_Order { BY_NAME, BY_DATE }

data class FilterPreferences(val sortOrder: sort_Order, val onlyFav: Boolean)

@Singleton
class preferenceManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("user_preferences")

    val preferenceFlow = dataStore.data
        .catch { exeption ->
            if (exeption is IOException) {
                Log.e(TAG, "Error reading preferences", exeption)
                emit(emptyPreferences())
            } else {
                throw exeption
            }
        }
        .map { preferences ->
            val sortOrder = sort_Order.valueOf(
                preferences[PreferenceKeys.SORT_ORDER] ?: sort_Order.BY_DATE.name
            )
            val onlyFav = preferences[PreferenceKeys.ONLY_FAV] ?: false

            FilterPreferences(sortOrder, onlyFav)
        }

    suspend fun updateSortOrder(sortOrder: sort_Order) {
        dataStore.edit { preferences -> preferences[PreferenceKeys.SORT_ORDER] = sortOrder.name }
    }

    suspend fun updateOnlyFav(onlyFav: Boolean) {
        dataStore.edit { preferences -> preferences[PreferenceKeys.ONLY_FAV] = onlyFav }
    }

    private object PreferenceKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val ONLY_FAV = preferencesKey<Boolean>("only_fav")
    }
}
package com.mobile.memorise.data.local.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobile.memorise.data.remote.dto.auth.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// Ekstensi untuk membuat DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val USER_KEY = stringPreferencesKey("user_data")

    // 1. Simpan User (Dipanggil saat Login/Register Sukses)
    suspend fun saveUser(user: UserDto) {
        context.dataStore.edit { preferences ->
            // Ubah Object UserDto menjadi String JSON
            val jsonString = Json.encodeToString(user)
            preferences[USER_KEY] = jsonString
        }
    }

    // 2. Ambil User (Dipanggil di ViewModel)
    val userData: Flow<UserDto?> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[USER_KEY]
            if (jsonString != null) {
                try {
                    // Ubah String JSON kembali ke Object UserDto
                    Json.decodeFromString<UserDto>(jsonString)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

    // 3. Hapus User (Dipanggil saat Logout)
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_KEY)
        }
    }
}
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

    // === TAMBAHAN PENTING ===
    // Konfigurasi Json agar lebih 'pemaaf' dan tidak mudah crash
    private val json = Json {
        ignoreUnknownKeys = true // Jika backend tambah field baru, aplikasi tidak crash
        encodeDefaults = true    // Menyertakan nilai default saat disimpan
        isLenient = true         // Lebih toleran terhadap format JSON yang agak berantakan
        coerceInputValues = true // PENTING: Jika ada null masuk ke field non-null, gunakan default value
    }

    // 1. Simpan User (Dipanggil saat Login/Register Sukses)
    suspend fun saveUser(user: UserDto) {
        context.dataStore.edit { preferences ->
            // Gunakan variabel 'json' custom kita, bukan Json default
            val jsonString = json.encodeToString(user)
            preferences[USER_KEY] = jsonString
        }
    }

    // 2. Ambil User (Dipanggil di ViewModel)
    val userData: Flow<UserDto?> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[USER_KEY]
            if (jsonString != null) {
                try {
                    // Gunakan variabel 'json' custom untuk membaca kembali
                    json.decodeFromString<UserDto>(jsonString)
                } catch (e: Exception) {
                    e.printStackTrace() // Log error di Logcat jika gagal parsing
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
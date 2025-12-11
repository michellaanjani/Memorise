package com.mobile.memorise.data.local.token

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobile.memorise.util.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
    }

    // Mendapatkan token yang sudah dideskripsi
    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        val encrypted = prefs[KEY_TOKEN]
        if (encrypted.isNullOrBlank()) null else cryptoManager.decrypt(encrypted)
    }

    suspend fun saveToken(token: String) {
        val encrypted = cryptoManager.encrypt(token)
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = encrypted
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
        }
    }
}
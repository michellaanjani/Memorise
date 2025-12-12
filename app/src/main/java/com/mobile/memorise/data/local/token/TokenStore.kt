// TokenStore.kt
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
import kotlinx.coroutines.flow.first
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
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    // --- Access Token ---
    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]?.let { cryptoManager.decrypt(it) }
    }

    // --- Refresh Token ---
    val refreshToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_REFRESH_TOKEN]?.let { cryptoManager.decrypt(it) }
    }

    // Fungsi helper untuk mengambil token secara Synchronous (penting untuk Authenticator)
    suspend fun getAccessTokenSync(): String? = accessToken.first()
    suspend fun getRefreshTokenSync(): String? = refreshToken.first()

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val encryptedAccess = cryptoManager.encrypt(accessToken)
        val encryptedRefresh = cryptoManager.encrypt(refreshToken)

        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = encryptedAccess
            prefs[KEY_REFRESH_TOKEN] = encryptedRefresh
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }
}
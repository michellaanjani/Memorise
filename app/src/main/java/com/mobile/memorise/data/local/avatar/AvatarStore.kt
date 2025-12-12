package com.mobile.memorise.data.local.avatar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.avatarDataStore: DataStore<Preferences> by preferencesDataStore(name = "avatar_prefs")

@Singleton
class AvatarStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_AVATAR_URI = stringPreferencesKey("avatar_uri")
    }

    val avatarUri: Flow<String?> = context.avatarDataStore.data.map { prefs ->
        prefs[KEY_AVATAR_URI]
    }

    suspend fun saveAvatarUri(uri: String?) {
        context.avatarDataStore.edit { prefs ->
            if (uri != null) {
                prefs[KEY_AVATAR_URI] = uri
            } else {
                prefs.remove(KEY_AVATAR_URI)
            }
        }
    }

    suspend fun clearAvatar() {
        context.avatarDataStore.edit { prefs ->
            prefs.remove(KEY_AVATAR_URI)
        }
    }
}


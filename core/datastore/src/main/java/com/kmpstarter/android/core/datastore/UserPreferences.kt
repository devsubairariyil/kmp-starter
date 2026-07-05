package com.kmpstarter.android.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "kmpstarter")

class UserPreferences
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }

    suspend fun setAccessToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) prefs.remove(ACCESS_TOKEN) else prefs[ACCESS_TOKEN] = token
        }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }
}

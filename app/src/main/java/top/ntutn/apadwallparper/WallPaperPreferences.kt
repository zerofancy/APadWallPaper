package top.ntutn.apadwallparper

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object WallPaperPreferences {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val uriHKey = stringPreferencesKey("bitmap_h_uri")
    private val uriVKey = stringPreferencesKey("bitmap_v_uri")

    fun getUriH(context: Context): Flow<Uri?> = context.dataStore.data.map { preferences ->
        preferences[uriHKey]?.toUri()
    }

    fun getUriV(context: Context): Flow<Uri?> = context.dataStore.data.map { preferences ->
        preferences[uriVKey]?.toUri()
    }

    suspend fun updateUriH(context: Context, value: Uri) = context.dataStore.edit { preferences ->
        preferences[uriHKey] = value.toString()
    }

    suspend fun updateUriV(context: Context, value: Uri) = context.dataStore.edit { preference ->
        preference[uriVKey] = value.toString()
    }
}
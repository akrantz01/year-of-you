package you.yearof.app.util

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class Preferences(
    @Provided private val paths: Paths,
) {
    private val dataStore = PreferenceDataStoreFactory.createWithPath {
        // TODO: look into effort required to use kotlinx instead of okio
        paths.inDocuments(DataStoreName).toString().toPath()
    }

    val user: Flow<CurrentUserInfo?> = dataStore.data.map { prefs ->
        val id = prefs[idKey]
        val username = prefs[usernameKey]
        val name = prefs[displayNameKey]
        if (id != null && username != null && name != null) CurrentUserInfo(id.toUInt(), username, name)
        else null
    }

    val accountOnboardingSeen: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[accountOnboardingSeenKey] ?: false
    }

    suspend fun setUser(id: UInt, username: String, name: String) {
        dataStore.edit { prefs ->
            prefs[idKey] = id.toInt()
            prefs[usernameKey] = username
            prefs[displayNameKey] = name
        }
    }

    suspend fun setAccountOnboardingSeen(seen: Boolean = true) {
        dataStore.edit { prefs ->
            prefs[accountOnboardingSeenKey] = seen
        }
    }

    suspend fun clearUser() {
        dataStore.edit { prefs ->
            prefs.remove(idKey)
            prefs.remove(usernameKey)
            prefs.remove(displayNameKey)
        }
    }

    companion object {
        private const val DataStoreName = "user-preferences.preferences_pb"

        private val idKey = intPreferencesKey("id")
        private val usernameKey = stringPreferencesKey("username")
        private val displayNameKey = stringPreferencesKey("displayName")
        private val accountOnboardingSeenKey = booleanPreferencesKey("accountOnboardingSeen")
    }
}

data class CurrentUserInfo(val id: UInt, val username: String, val name: String)

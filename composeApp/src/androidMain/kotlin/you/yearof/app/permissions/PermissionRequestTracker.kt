package you.yearof.app.permissions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

class PermissionRequestTracker(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun wasRequested(permission: Permission): Boolean = dataStore.data.first()[permission.toKey()] ?: false

    suspend fun markRequested(permission: Permission) {
        dataStore.edit { preferences ->
            preferences[permission.toKey()] = true
        }
    }
}

private val CameraKey = booleanPreferencesKey("camera")
private val NotificationKey = booleanPreferencesKey("notification")

private fun Permission.toKey() =
    when (this) {
        Permission.Camera -> CameraKey
        Permission.Notification -> NotificationKey
    }

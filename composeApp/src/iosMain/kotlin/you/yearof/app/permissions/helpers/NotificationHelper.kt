package you.yearof.app.permissions.helpers

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import you.yearof.app.permissions.PermissionStatus
import you.yearof.app.util.Log

class NotificationHelper : PermissionHelper {
    override fun request(onResult: (Boolean) -> Unit) {
        handleRequest(
            onResult = onResult,
            launchRequest = {
                notificationCenter()
                    .requestAuthorizationWithOptions(
                        UNAuthorizationOptionSound
                            .or(UNAuthorizationOptionAlert)
                            .or(UNAuthorizationOptionBadge)
                    ) { ok, error ->
                        onResult(ok && error == null)
                    }
            }
        )
    }

    override fun read(onResult: (PermissionStatus) -> Unit) {
        notificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                onResult(when (settings?.authorizationStatus) {
                    UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral -> PermissionStatus.Granted
                    UNAuthorizationStatusNotDetermined -> PermissionStatus.Unknown
                    UNAuthorizationStatusDenied -> PermissionStatus.Denied
                    else -> {
                        Log.warn("Permission.NotificationHelper", "Unknown permission status: ${settings?.authorizationStatus}")
                        PermissionStatus.Denied
                    }
                })
            }
    }

    private fun notificationCenter() = UNUserNotificationCenter.currentNotificationCenter()
}
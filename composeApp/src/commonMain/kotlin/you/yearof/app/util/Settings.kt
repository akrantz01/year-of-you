package you.yearof.app.util

import androidx.compose.runtime.Composable

enum class SettingsDeepLink {
    Notifications,
}

interface SettingsAccess {
    fun open(deepLink: SettingsDeepLink? = null)
}

@Composable
expect fun rememberSettingsAccess(): SettingsAccess

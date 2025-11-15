package you.yearof.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSettingsAccess(): SettingsAccess {
    val context = LocalContext.current
    return remember { AndroidSettingsAccess(context) }
}

@Stable
internal class AndroidSettingsAccess(
    private val context: Context,
) : SettingsAccess {
    override fun open(deepLink: SettingsDeepLink?) {
        val intent =
            when (deepLink) {
                null ->
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                SettingsDeepLink.Notifications ->
                    Intent().apply {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
            }
        context.startActivity(intent)
    }
}

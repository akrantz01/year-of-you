package you.yearof.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun rememberSettingsAccess(): SettingsAccess = remember { IosSettingsAccess() }

@Stable
internal class IosSettingsAccess : SettingsAccess {
    override fun open(deepLink: SettingsDeepLink?) {
        val url =
            NSURL.URLWithString(
                when (deepLink) {
                    null -> UIApplicationOpenSettingsURLString
                    SettingsDeepLink.Notifications -> UIApplicationOpenNotificationSettingsURLString
                },
            ) ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>(), null)
    }
}

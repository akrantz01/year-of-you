package you.yearof.app.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build

internal fun Permission.toAndroid(): String =
    when (this) {
        Permission.Camera -> Manifest.permission.CAMERA
        Permission.Notification ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else {
                ""
            }
    }

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    error("Permissions should be called in the context of an Activity")
}

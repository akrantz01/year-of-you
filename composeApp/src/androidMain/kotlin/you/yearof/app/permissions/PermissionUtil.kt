package you.yearof.app.permissions

import android.Manifest
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

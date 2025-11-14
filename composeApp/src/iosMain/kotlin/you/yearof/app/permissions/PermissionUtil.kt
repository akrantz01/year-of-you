package you.yearof.app.permissions

import platform.AVFoundation.AVMediaTypeVideo
import you.yearof.app.permissions.helpers.CameraHelper
import you.yearof.app.permissions.helpers.NotificationHelper

internal fun Permission.toHelper() =
    when (this) {
        Permission.Camera -> CameraHelper(AVMediaTypeVideo)
        Permission.Notification -> NotificationHelper()
    }

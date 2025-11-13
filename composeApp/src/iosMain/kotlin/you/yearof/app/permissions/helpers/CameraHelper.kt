package you.yearof.app.permissions.helpers

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import you.yearof.app.permissions.PermissionStatus
import you.yearof.app.util.Log

class CameraHelper(private val type: AVMediaType) : PermissionHelper {
    override fun request(onResult: (Boolean) -> Unit) {
        handleRequest(
            onResult = onResult,
            launchRequest = {
                AVCaptureDevice.requestAccessForMediaType(type) {
                    onResult(it)
                }
            }
        )
    }

    override fun read(onResult: (PermissionStatus) -> Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(type)
        onResult(when (status) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.Granted
            AVAuthorizationStatusNotDetermined -> PermissionStatus.Unknown
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> PermissionStatus.Denied
            else -> {
                Log.warn("Permissions.CameraHelper", "Unknown permission status: $status")
                PermissionStatus.Denied
            }
        })
    }
}

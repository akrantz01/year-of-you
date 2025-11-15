package you.yearof.app.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

enum class Permission {
    Camera,
    Notification,
}

enum class PermissionStatus {
    Loading,
    Unknown,
    Granted,
    Denied,
    PermanentlyDenied,
}

@Stable
interface PermissionState {
    val permission: Permission

    val status: PermissionStatus

    fun request()
}

@Stable
internal interface RefreshablePermissionState : PermissionState {
    fun refresh()
}

@Composable
expect fun rememberPermissionState(permission: Permission): PermissionState

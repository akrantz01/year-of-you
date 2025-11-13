package you.yearof.app.permissions

import androidx.compose.runtime.Composable

enum class Permission {
    Camera, Notification
}

enum class PermissionStatus {
    Loading, Unknown, Granted, Denied
}

interface PermissionState {
    val permission: Permission

    val status: PermissionStatus

    fun request()
}

internal interface RefreshablePermissionState : PermissionState {
    fun refresh()
}

@Composable
expect fun rememberPermissionState(permission: Permission): PermissionState

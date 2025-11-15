package you.yearof.app.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

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

/**
 * Bridges a composable-scoped [PermissionState] to a lifecycle-safe [PermissionHandle]. The handle
 * exposes the current status through a [StateFlow], making it consumable from view models or other
 * long-lived classes while still delegating permission requests back to the underlying
 * [PermissionState].
 */
@Composable
fun rememberPermissionHandle(permission: Permission): PermissionHandle {
    val state = rememberPermissionState(permission)
    val statusFlow = remember(permission) { MutableStateFlow(state.status) }

    LaunchedEffect(state) {
        snapshotFlow { state.status }.collect { status ->
            statusFlow.value = status
        }
    }

    return remember(state, statusFlow) {
        PermissionStateHandle(state, statusFlow)
    }
}

@Stable
private class PermissionStateHandle(
    private val state: PermissionState,
    override val status: StateFlow<PermissionStatus>,
) : PermissionHandle {
    override val permission: Permission = state.permission

    override fun request() {
        state.request()
    }
}

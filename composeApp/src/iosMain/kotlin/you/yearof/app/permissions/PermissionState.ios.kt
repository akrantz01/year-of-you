package you.yearof.app.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
actual fun rememberPermissionState(permission: Permission): PermissionState {
    val scope = rememberCoroutineScope()
    val state =
        remember(permission) {
            IosPermissionState(permission, scope)
        }

    PermissionLifecycleEffect(state)

    return state
}

internal class IosPermissionState(
    override val permission: Permission,
    private val scope: CoroutineScope,
) : RefreshablePermissionState {
    private val helper = permission.toHelper()

    override var status by mutableStateOf(PermissionStatus.Loading)

    init {
        refresh()
    }

    override fun request() {
        helper.request {
            refresh()
        }
    }

    override fun refresh() {
        helper.read { status ->
            scope.launch(Dispatchers.Main) {
                this@IosPermissionState.status = status
            }
        }
    }
}

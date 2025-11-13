package you.yearof.app.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal fun PermissionLifecycleEffect(state: RefreshablePermissionState, lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME) {
    val observer = remember(state) {
        LifecycleEventObserver { _, event ->
            if (event == lifecycleEvent && state.status != PermissionStatus.Granted)
                state.refresh()
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, observer) {
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

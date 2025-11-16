package you.yearof.app.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val Context.permissionRequestDataStore by preferencesDataStore("permission-requests")

@Composable
actual fun rememberPermissionState(permission: Permission): PermissionState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tracker = remember { PermissionRequestTracker(context.permissionRequestDataStore) }
    val permissionState =
        remember(permission) {
            AndroidPermissionState(permission, context, scope, context.findActivity(), tracker)
        }

    PermissionLifecycleEffect(permissionState)

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionState.refresh()
        }

    DisposableEffect(permissionState, launcher) {
        permissionState.launcher = launcher
        onDispose {
            permissionState.launcher = null
        }
    }

    return permissionState
}

@Stable
internal class AndroidPermissionState(
    override val permission: Permission,
    private val context: Context,
    private val scope: CoroutineScope,
    private val activity: Activity,
    private val tracker: PermissionRequestTracker,
) : RefreshablePermissionState {
    private val androidPermission = permission.toAndroid()

    override var status by mutableStateOf(PermissionStatus.Loading)

    internal var launcher: ActivityResultLauncher<String>? = null

    init {
        refresh()
    }

    override fun request() {
        scope.launch { tracker.markRequested(permission) }
        if (androidPermission.isEmpty()) {
            refresh()
        } else {
            checkNotNull(launcher).launch(androidPermission)
        }
    }

    override fun refresh() {
        scope.launch {
            val requested = tracker.wasRequested(permission)
            status = readStatus(requested)
        }
    }

    private fun readStatus(alreadyRequested: Boolean): PermissionStatus {
        if (androidPermission.isEmpty()) return PermissionStatus.Granted

        val hasPermission =
            ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            return PermissionStatus.Granted
        } else if (!alreadyRequested) {
            return PermissionStatus.Unknown
        }

        val permanent = ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermission)
        return if (permanent) PermissionStatus.PermanentlyDenied else PermissionStatus.Denied
    }
}

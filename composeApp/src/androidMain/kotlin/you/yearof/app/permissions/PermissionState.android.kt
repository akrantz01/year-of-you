package you.yearof.app.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberPermissionState(permission: Permission): PermissionState {
    val context = LocalContext.current
    val permissionState = remember(permission) {
        AndroidPermissionState(permission, context)
    }

    PermissionLifecycleEffect(permissionState)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
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

internal class AndroidPermissionState(override val permission: Permission, private val context: Context) : RefreshablePermissionState {
    private val androidPermission = permission.toAndroid()

    override var status by mutableStateOf(readStatus())

    internal var launcher: ActivityResultLauncher<String>? = null

    override fun request() {
        if (androidPermission.isEmpty()) refresh()
        else launcher?.launch(androidPermission) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
    }

    override fun refresh() {
        status = readStatus()
    }

    private fun readStatus(): PermissionStatus {
        if (androidPermission.isEmpty()) return PermissionStatus.Granted

        val hasPermission = ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED
        return if (hasPermission) PermissionStatus.Granted else PermissionStatus.Denied
    }
}

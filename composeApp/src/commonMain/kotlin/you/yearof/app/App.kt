package you.yearof.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState

@Composable
fun App() {
    MaterialTheme {
        val greeting = remember { Greeting().greet() }
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer).safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Compose: $greeting")
            EnsureCameraPermissions {
                Camera()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EnsureCameraPermissions(content: @Composable () -> Unit) {
    val cameraPermission = rememberPermissionState(Permission.Camera)

    if (cameraPermission.status.isGranted) {
        content()
    } else {
        LaunchedEffect(Unit) {
            cameraPermission.launchPermissionRequest()
        }
    }
}

@Composable
fun Camera() {
    var lens by remember { mutableStateOf(CameraLens.Back) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lens = lens,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(onClick = {
                lens = lens.opposite()
            }) {
                Text("Switch Camera")
            }
        }
    }
}

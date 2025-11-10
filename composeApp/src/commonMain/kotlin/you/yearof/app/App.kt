package you.yearof.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import you.yearof.app.camera.CameraPreview
import you.yearof.app.camera.rememberCameraController

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
    val scope = rememberCoroutineScope()

    val controller = rememberCameraController()
    val isReady by controller.isReady.collectAsState()

    Text("Ready: $isReady")
//    Text("Capture: $state")
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            controller = controller,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = {
                    scope.launch {
                        controller.updateConfiguration { config ->
                            config.copy(position = config.position.opposite())
                        }
                    }
                }) {
                    Text("Switch")
                }

                Button(onClick = {
                    scope.launch {
                        controller.capture()
                    }
                }) {
                    Text("Capture")
                }
            }
        }
    }
}

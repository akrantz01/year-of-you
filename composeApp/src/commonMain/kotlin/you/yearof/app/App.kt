package you.yearof.app

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.*
import com.kashif.cameraK.permissions.providePermissions
import com.kashif.cameraK.ui.CameraPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            Camera()
        }
    }
}

@Composable
fun Camera() {
    val permissions = providePermissions()
    var cameraPermissionState by remember { mutableStateOf(permissions.hasCameraPermission()) }

    if (cameraPermissionState) {
        ShowCamera()
    } else {
        permissions.RequestCameraPermission(
            onGranted = { cameraPermissionState = true },
            onDenied = { println("Camera permission denied") })
    }
}

@Composable
fun ShowCamera() {
    val scope = rememberCoroutineScope()

    var cameraController by remember { mutableStateOf<CameraController?>(null) }

    var overlayAngle by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = overlayAngle,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing,
        ),
        label = "cameraFlip"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 32 * density
                },
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraConfiguration = {
                    setCameraLens(CameraLens.BACK)
                    setFlashMode(FlashMode.AUTO)
                    setImageFormat(ImageFormat.JPEG)
                    setDirectory(Directory.PICTURES)
                    setTorchMode(TorchMode.OFF)
                    setQualityPrioritization(QualityPrioritization.QUALITY)
                },
                onCameraControllerReady = { cameraController = it },
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
                cameraController?.let { controller ->
                    scope.launch {
                        overlayAngle += 180f

                        delay(300)
                        controller.toggleCameraLens()
                    }
                }
            }) {
                Text("Switch Camera")
            }
        }
    }
}

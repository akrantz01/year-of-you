package you.yearof.app.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
actual fun CameraPreview(modifier: Modifier, controller: CameraController) {
    val surfaceRequest by controller.camera.surfaceRequests.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        surfaceRequest?.let { req ->
            CameraXViewfinder(
                surfaceRequest = req,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
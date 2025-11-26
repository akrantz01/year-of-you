package you.yearof.app.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    val surfaceRequest by controller.camera.surfaceRequests.collectAsState()

    PreviewAspectRatio(modifier = modifier) { inner ->
        surfaceRequest?.let { req ->
            Box(modifier = inner) {
                CameraXViewfinder(
                    surfaceRequest = req,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

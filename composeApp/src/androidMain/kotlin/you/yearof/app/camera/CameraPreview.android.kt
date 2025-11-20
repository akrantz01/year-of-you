package you.yearof.app.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    val surfaceRequest by controller.camera.surfaceRequests.collectAsState()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        surfaceRequest?.let { req ->
            val previewAspectRatio = 3f / 4f
            val previewModifier =
                if (maxHeight * previewAspectRatio <= maxWidth) {
                    Modifier
                        .fillMaxHeight()
                        .aspectRatio(previewAspectRatio)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(previewAspectRatio)
                }

            Box(modifier = previewModifier) {
                CameraXViewfinder(
                    surfaceRequest = req,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

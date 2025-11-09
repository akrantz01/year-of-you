package you.yearof.app.camera

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

@Composable
expect fun rememberCameraController(): CameraController

expect class CameraController {
    val configuration: StateFlow<CameraConfiguration>
    val captureState: StateFlow<CaptureState>
    val isReady: StateFlow<Boolean>

    suspend fun attach()
    fun detach()
    suspend fun updateConfiguration(update: (CameraConfiguration) -> CameraConfiguration)
    suspend fun takePhoto(): Photo
    suspend fun takeDualPhoto(): Map<CameraPosition, Photo>
}
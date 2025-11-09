package you.yearof.app.camera

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

@Composable
expect fun rememberCameraController(): CameraController

open class AbstractCameraController {
    internal val configuration = MutableStateFlow(CameraConfiguration())
    val currentConfiguration: StateFlow<CameraConfiguration> = configuration.asStateFlow()

    internal val captureState = MutableStateFlow(CaptureState.Idle)
    val currentCaptureState: StateFlow<CaptureState> = captureState.asStateFlow()

    protected val isReady = MutableStateFlow(false)
    val isReadyNow: StateFlow<Boolean> = isReady.asStateFlow()

    internal suspend fun waitIdle() {
        captureState.first { it == CaptureState.Idle }
    }

    internal suspend fun waitReady(requireUnready: Boolean = false) {
        if (requireUnready) isReady.first { !it }
        isReady.first { it }
    }
}

expect class CameraController : AbstractCameraController {
    suspend fun attach()
    fun detach()
    suspend fun updateConfiguration(update: (CameraConfiguration) -> CameraConfiguration)
    suspend fun takePhoto(): Photo
}

suspend fun CameraController.takeDualPhoto(): Map<CameraPosition, Photo> {
    waitIdle()
    waitReady()

    val config = configuration.value

    // capture first photo
    captureState.value = CaptureState.First
    val firstPosition = config.position
    val first = takePhoto()

    // swap lenses
    captureState.value = CaptureState.Switching
    val secondPosition = config.position.opposite()
    configuration.value = config.copy(position = secondPosition)
    waitReady(requireUnready = true)

    // capture second photo
    captureState.value = CaptureState.Second
    val second = takePhoto()

    // restore original config
    configuration.value = config
    waitReady(requireUnready = true)
    captureState.value = CaptureState.Idle

    return mapOf(firstPosition to first, secondPosition to second)
}

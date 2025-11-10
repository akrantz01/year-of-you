package you.yearof.app.camera

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

@Composable
expect fun rememberCamera(): Camera

abstract class AbstractCamera {
    internal val configuration = MutableStateFlow(CameraConfiguration())

    internal val isReady = MutableStateFlow(false)

    fun updateConfiguration(update: (CameraConfiguration) -> CameraConfiguration) = configuration.update(update)

    internal suspend fun waitReady(requireUnready: Boolean = false) {
        if (requireUnready) isReady.first { !it }
        isReady.first { it }
    }
}

expect class Camera : AbstractCamera {
    suspend fun attach()
    fun detach()
    suspend fun captureImage(): Photo
}

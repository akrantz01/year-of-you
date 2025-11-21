package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

@Composable
expect fun rememberCamera(): Camera

@Stable
open class AbstractCamera {
    internal val configuration = MutableStateFlow(CameraConfiguration())

    internal val isReady = MutableStateFlow(false)

    fun updateConfiguration(update: (CameraConfiguration) -> CameraConfiguration) = configuration.update(update)

    internal suspend fun waitReady(requireUnready: Boolean = false) {
        if (requireUnready) isReady.first { !it }
        isReady.first { it }
    }

    protected fun photoName(position: CameraPosition): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "capture-$position-$timestamp.jpeg"
    }
}

@Stable
expect class Camera : AbstractCamera {
    suspend fun attach()

    fun detach()

    suspend fun captureImage(): String
}

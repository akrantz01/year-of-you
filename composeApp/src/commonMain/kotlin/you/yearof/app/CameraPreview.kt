package you.yearof.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow

enum class CameraLens {
    Front, Back;

    fun opposite() = when (this) {
        Front -> Back
        Back -> Front
    }
}

data class PhotoResult(
    val bytes: ByteArray,
    val rotation: Int
)

class CameraController internal constructor() {
    internal val requests = MutableSharedFlow<CompletableDeferred<PhotoResult>>(extraBufferCapacity = 1)

    suspend fun takePhoto(): PhotoResult {
        val deferred = CompletableDeferred<PhotoResult>()
        requests.emit(deferred)
        return deferred.await()
    }
}

@Composable
fun rememberCameraController(): CameraController = remember { CameraController() }

@Composable
expect fun CameraPreview(modifier: Modifier = Modifier, controller: CameraController, lens: CameraLens)

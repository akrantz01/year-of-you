package you.yearof.app.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberCameraController(): CameraController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    return remember {
        CameraController(context, lifecycleOwner)
    }
}

actual class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) {
    private val _configuration = MutableStateFlow(CameraConfiguration())
    actual val configuration = _configuration.asStateFlow()

    private val _captureState = MutableStateFlow(CaptureState.Idle)
    actual val captureState = _captureState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    actual val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var executor = Executors.newSingleThreadExecutor()

    internal val surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null

    actual suspend fun attach() {
        cameraProvider = ProcessCameraProvider.awaitInstance(context)
        configuration.collectLatest { config ->
            coroutineScope {
                _isReady.value = false
                val camera = bindCamera(config)

                launch {
                    camera?.cameraInfo?.cameraState?.asFlow()
                        ?.first { it.type == CameraState.Type.OPEN }
                    _isReady.value = true
                }
            }
        }
    }

    actual fun detach() {
        cameraProvider?.unbindAll()
        executor.shutdown()
    }

    private fun bindCamera(config: CameraConfiguration): Camera? {
        val provider = cameraProvider ?: return null

        val selector = when (config.position) {
            CameraPosition.Front -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraPosition.Back -> CameraSelector.DEFAULT_BACK_CAMERA
        }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build().apply {
                flashMode = when (config.flashMode) {
                    FlashMode.Off -> ImageCapture.FLASH_MODE_OFF
                    FlashMode.Auto -> ImageCapture.FLASH_MODE_AUTO
                    FlashMode.On -> ImageCapture.FLASH_MODE_ON
                }
            }
        imageCapture = capture

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider {
                surfaceRequests.value = it
            }
        }

        provider.unbindAll()
        return provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
    }

    actual suspend fun updateConfiguration(update: (CameraConfiguration) -> CameraConfiguration) {
        _captureState.first { it == CaptureState.Idle }
        _configuration.update(update)
    }

    actual suspend fun takeDualPhoto(): Map<CameraPosition, Photo> {
        _captureState.first { it == CaptureState.Idle }
        _isReady.first { it }

        val config = _configuration.value

        // capture first photo
        _captureState.value = CaptureState.First
        val firstPosition = config.position
        val first = takePhoto()

        // swap lenses
        _captureState.value = CaptureState.Switching
        val secondPosition = config.position.opposite()
        _configuration.value = config.copy(position = secondPosition)
        _isReady.first { it }

        // capture second photo
        _captureState.value = CaptureState.Second
        val second = takePhoto()

        // restore original config
        _configuration.value = config
        _isReady.first { it }
        _captureState.value = CaptureState.Idle

        return mapOf(firstPosition to first, secondPosition to second)
    }

    actual suspend fun takePhoto(): Photo {
        _isReady.first { it }

        return suspendCancellableCoroutine { cont ->
            val capture = imageCapture ?: run {
                cont.resumeWithException(IllegalStateException("Camera not initialized"))
                return@suspendCancellableCoroutine
            }

            val outputStream = ByteArrayOutputStream()

            capture.takePicture(
                ImageCapture.OutputFileOptions.Builder(outputStream).build(),
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        cont.resumeWith(Result.success(outputStream.toByteArray()))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWithException(exception)
                    }
                }
            )
        }
    }
}

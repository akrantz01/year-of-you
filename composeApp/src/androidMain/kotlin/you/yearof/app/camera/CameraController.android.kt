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
) : AbstractCameraController() {
    private var executor = Executors.newSingleThreadExecutor()

    internal val surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null

    actual suspend fun attach() {
        cameraProvider = ProcessCameraProvider.awaitInstance(context)
        currentConfiguration.collectLatest { config ->
            coroutineScope {
                isReady.value = false
                val camera = bindCamera(config) ?: return@coroutineScope

                launch {
                    camera.cameraInfo.cameraState.asFlow().first { it.type == CameraState.Type.OPEN }
                    isReady.value = true
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
        waitIdle()
        configuration.update(update)
    }

    actual suspend fun takePhoto(): Photo {
        waitReady()

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

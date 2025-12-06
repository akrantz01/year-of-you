package you.yearof.app.camera

import android.content.Context
import android.os.Build
import android.util.LayoutDirection
import android.util.Rational
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.uuid.Uuid

@Composable
actual fun rememberCamera(): Camera {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    return remember {
        Camera(context, lifecycleOwner)
    }
}

@Stable
actual class Camera(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
) : AbstractCamera() {
    private val executor = Executors.newSingleThreadExecutor()
    private val windowService = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    internal val surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)

    private var provider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null

    actual suspend fun attach() {
        provider = ProcessCameraProvider.awaitInstance(context)
        configuration.collectLatest { config ->
            coroutineScope {
                isReady.value = false
                val camera = bindCamera(config) ?: return@coroutineScope

                launch {
                    camera.cameraInfo.cameraState
                        .asFlow()
                        .first { it.type == CameraState.Type.OPEN }
                    isReady.value = true
                }
            }
        }
    }

    actual fun detach() {
        surfaceRequests.value?.willNotProvideSurface()
        surfaceRequests.value = null
        provider?.unbindAll()
        executor.shutdown()
    }

    private fun bindCamera(config: CameraConfiguration): androidx.camera.core.Camera? {
        val cameraProvider = provider ?: return null
        val displayRotation = displayRotation()

        val selector =
            when (config.position) {
                CameraPosition.Front -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraPosition.Back -> CameraSelector.DEFAULT_BACK_CAMERA
            }

        val aspectRatioSelector =
            ResolutionSelector
                .Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()

        val capture =
            ImageCapture
                .Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(displayRotation)
                .setResolutionSelector(aspectRatioSelector)
                .setFlashMode(
                    when (config.flashMode) {
                        FlashMode.Off -> ImageCapture.FLASH_MODE_OFF
                        FlashMode.Auto -> ImageCapture.FLASH_MODE_AUTO
                        FlashMode.On -> ImageCapture.FLASH_MODE_ON
                    },
                ).build()
        imageCapture = capture

        val preview =
            Preview
                .Builder()
                .setResolutionSelector(aspectRatioSelector)
                .build()
                .apply {
                    setSurfaceProvider { surfaceRequest ->
                        surfaceRequests.update { surfaceRequest }
                    }
                }

        val aspectRatio = Rational(3, 4)
        val viewPort =
            ViewPort
                .Builder(aspectRatio, displayRotation)
                .setLayoutDirection(LayoutDirection.LTR)
                .setScaleType(ViewPort.FIT)
                .build()

        val useCase =
            UseCaseGroup
                .Builder()
                .addUseCase(preview)
                .addUseCase(capture)
                .setViewPort(viewPort)
                .build()

        cameraProvider.unbindAll()
        return cameraProvider.bindToLifecycle(lifecycleOwner, selector, useCase)
    }

    actual suspend fun captureImage(): String =
        suspendCancellableCoroutine { cont ->
            val capture =
                imageCapture ?: run {
                    cont.resumeWithException(IllegalStateException("Camera not initialized"))
                    return@suspendCancellableCoroutine
                }

            val output = File(context.cacheDir, "${Uuid.random()}.jpeg")

            capture.takePicture(
                ImageCapture.OutputFileOptions
                    .Builder(output)
                    .setMetadata(
                        ImageCapture.Metadata().apply {
                            isReversedHorizontal = true
                        },
                    ).build(),
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        cont.resume(output.absolutePath)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWithException(exception)
                    }
                },
            )
        }

    private fun displayRotation(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display.rotation
        } else {
            @Suppress("DEPRECATION")
            windowService.defaultDisplay.rotation
        }
}

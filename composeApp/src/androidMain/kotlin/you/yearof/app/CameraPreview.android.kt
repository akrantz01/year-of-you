package you.yearof.app

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@Composable
actual fun CameraPreview(modifier: Modifier, controller: CameraController, lens: CameraLens) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    val surfaceRequests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val surfaceRequest by surfaceRequests.collectAsState(initial = null)

    val selector = remember(lens) {
        when (lens) {
            CameraLens.Front -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraLens.Back -> CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    val capture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val provider by produceState<ProcessCameraProvider?>(null, context) {
        value = ProcessCameraProvider.awaitInstance(context)
    }

    DisposableEffect(provider, lifecycleOwner, selector) {
        val p = provider
        if (p != null) {
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider { request ->
                    surfaceRequests.value = request
                }
            }
            p.unbindAll()
            p.bindToLifecycle(lifecycleOwner, selector, preview, capture)
        }

        onDispose {
            provider?.unbindAll()
        }
    }

    LaunchedEffect(controller, capture) {
        controller.requests.collectLatest { deferred ->
            val outputStream = ByteArrayOutputStream()

            capture.takePicture(
                ImageCapture.OutputFileOptions.Builder(outputStream).build(),
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        deferred.complete(PhotoResult(outputStream.toByteArray()))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        deferred.completeExceptionally(exception)
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        surfaceRequest?.let { req ->
            CameraXViewfinder(
                surfaceRequest = req,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

package you.yearof.app

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
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

@Composable
actual fun CameraPreview(modifier: Modifier, lens: CameraLens) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val surfaceRequests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val surfaceRequest by surfaceRequests.collectAsState(initial = null)

    val selector = remember(lens) {
        when (lens) {
            CameraLens.Front -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraLens.Back -> CameraSelector.DEFAULT_BACK_CAMERA
        }
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
            p.bindToLifecycle(lifecycleOwner, selector, preview)
        }

        onDispose {
            provider?.unbindAll()
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

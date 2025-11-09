package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFoundation.*
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberCameraController(): CameraController = remember { CameraController() }

@OptIn(ExperimentalForeignApi::class)
actual class CameraController {
    private val _configuration = MutableStateFlow(CameraConfiguration())
    actual val configuration: StateFlow<CameraConfiguration> = _configuration.asStateFlow()

    private val _captureState = MutableStateFlow(CaptureState.Idle)
    actual val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    actual val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    internal val session = AVCaptureSession()
    private val output = AVCapturePhotoOutput()
    private val captureDelegate = CaptureDelegate()

    actual suspend fun attach() = withContext(Dispatchers.IO) {
        session.sessionPreset = AVCaptureSessionPresetPhoto
        session.addOutput(output)
        session.startRunning()

        configuration.collectLatest { config ->
            _isReady.value = false
            configureSession(config)
            _isReady.value = true
        }
    }

    actual fun detach() {
        session.stopRunning()
    }

    private fun configureSession(config: CameraConfiguration) {
        session.beginConfiguration()

        session.inputs.forEach { input ->
            session.removeInput(input as AVCaptureInput)
        }

        session.addInput(createInputDevice(config))

        session.commitConfiguration()
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
            captureDelegate.onCapture = { result ->
                cont.resumeWith(Result.success(result))
            }

            val settings = AVCapturePhotoSettings.photoSettingsWithFormat(mapOf(
                AVVideoCodecKey to AVVideoCodecTypeJPEG
            ))

            settings.flashMode = when (_configuration.value.flashMode) {
                FlashMode.Off -> AVCaptureFlashModeOff
                FlashMode.Auto -> AVCaptureFlashModeAuto
                FlashMode.On -> AVCaptureFlashModeOn
            }

            output.capturePhotoWithSettings(settings, captureDelegate)
        }
    }
}

private class CaptureDelegate : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
    var onCapture: ((Photo) -> Unit)? = null

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        if (error != null) throw RuntimeException(error.localizedDescription)

        val data = didFinishProcessingPhoto.fileDataRepresentation()?.toByteArray()
        if (data != null) onCapture?.invoke(data)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun createInputDevice(config: CameraConfiguration): AVCaptureDeviceInput {
    val device = AVCaptureDevice.defaultDeviceWithDeviceType(
        deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
        mediaType = AVMediaTypeVideo,
        position = when (config.position) {
            CameraPosition.Front -> AVCaptureDevicePositionFront
            CameraPosition.Back -> AVCaptureDevicePositionBack
        },
    )!!

    if (device.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus)) {
        try {
            device.lockForConfiguration(null)
            device.focusMode = AVCaptureFocusModeContinuousAutoFocus
            device.unlockForConfiguration()
        } catch (t: Throwable) {
            println("could not configure autofocus: ${t.message}")
        }
    }

    return AVCaptureDeviceInput.deviceInputWithDevice(device, null)!!
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = memScoped {
    val buffer = ByteArray(length.toInt())
    buffer.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
    buffer
}

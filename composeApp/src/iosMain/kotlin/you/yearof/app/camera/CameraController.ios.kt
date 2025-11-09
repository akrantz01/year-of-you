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
actual class CameraController : AbstractCameraController() {
    internal val session = AVCaptureSession()
    private val output = AVCapturePhotoOutput()
    private val captureDelegate = CaptureDelegate()

    actual suspend fun attach() = withContext(Dispatchers.IO) {
        session.sessionPreset = AVCaptureSessionPresetPhoto
        session.addOutput(output)
        session.startRunning()

        currentConfiguration.collectLatest { config ->
            isReady.value = false
            configureSession(config)
            isReady.value = true
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
        captureState.first { it == CaptureState.Idle }
        configuration.update(update)
    }

    actual suspend fun takePhoto(): Photo {
        isReady.first { it }
        return suspendCancellableCoroutine { cont ->
            captureDelegate.onCapture = { result ->
                cont.resumeWith(Result.success(result))
            }

            val settings = AVCapturePhotoSettings.photoSettingsWithFormat(mapOf(
                AVVideoCodecKey to AVVideoCodecTypeJPEG
            ))

            settings.flashMode = when (configuration.value.flashMode) {
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

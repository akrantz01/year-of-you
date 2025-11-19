package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureFlashModeOn
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.focusMode
import platform.AVFoundation.isFocusModeSupported
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberCamera(): Camera = remember { Camera() }

@Stable
@OptIn(ExperimentalForeignApi::class)
actual class Camera : AbstractCamera() {
    internal val session = AVCaptureSession()
    private val output = AVCapturePhotoOutput()
    private val captureDelegate = CaptureDelegate()

    actual suspend fun attach() =
        withContext(Dispatchers.IO) {
            session.sessionPreset = AVCaptureSessionPresetPhoto
            session.addOutput(output)
            session.startRunning()

            configuration.collectLatest { config ->
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

    actual suspend fun captureImage(): String =
        suspendCancellableCoroutine { cont ->
            captureDelegate.onCapture = {
                captureDelegate.clear()
                // TODO: save result to file either here or within the delegate
                cont.resume("TODO")
            }
            captureDelegate.onError = { error ->
                captureDelegate.clear()
                cont.resumeWithException(error)
            }

            cont.invokeOnCancellation {
                captureDelegate.clear()
            }

            val settings =
                AVCapturePhotoSettings.photoSettingsWithFormat(
                    mapOf(AVVideoCodecKey to AVVideoCodecTypeJPEG),
                )

            settings.flashMode =
                when (configuration.value.flashMode) {
                    FlashMode.Off -> AVCaptureFlashModeOff
                    FlashMode.Auto -> AVCaptureFlashModeAuto
                    FlashMode.On -> AVCaptureFlashModeOn
                }

            output.capturePhotoWithSettings(settings, captureDelegate)
        }
}

private class CaptureDelegate :
    NSObject(),
    AVCapturePhotoCaptureDelegateProtocol {
    var onCapture: ((Photo) -> Unit)? = null
    var onError: ((Throwable) -> Unit)? = null

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        if (error != null) {
            onError?.invoke(RuntimeException(error.localizedDescription))
            return
        }

        val data = didFinishProcessingPhoto.fileDataRepresentation()?.toByteArray()
        if (data != null) {
            onCapture?.invoke(data)
        } else {
            onError?.invoke(IllegalStateException("No photo data returned"))
        }
    }

    fun clear() {
        onCapture = null
        onError = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun createInputDevice(config: CameraConfiguration): AVCaptureDeviceInput {
    val device =
        AVCaptureDevice.defaultDeviceWithDeviceType(
            deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
            mediaType = AVMediaTypeVideo,
            position =
                when (config.position) {
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
private fun NSData.toByteArray(): ByteArray =
    memScoped {
        val buffer = ByteArray(length.toInt())
        buffer.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
        buffer
    }

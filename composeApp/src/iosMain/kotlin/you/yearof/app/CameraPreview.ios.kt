package you.yearof.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readValue
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.focusMode
import platform.AVFoundation.isFocusModeSupported
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(modifier: Modifier, controller: CameraController, lens: CameraLens) {
    var captureSession by remember { mutableStateOf<AVCaptureSession?>(null) }
    val captureDelegate = remember { CaptureDelegate() }
    val output = remember { AVCapturePhotoOutput() }

    LaunchedEffect(lens) {
        captureSession?.let { session ->
            session.stopRunning()
            session.beginConfiguration()

            session.inputs.forEach { input ->
                session.removeInput(input as AVCaptureInput)
            }

            session.addInput(createInputDevice(lens))

            session.commitConfiguration()
            session.startRunning()
        }
    }

    LaunchedEffect(controller) {
        controller.requests.collect { deferred ->
            captureDelegate.onCapture = { result ->
                deferred.complete(result)
            }

            val settings = AVCapturePhotoSettings.photoSettingsWithFormat(mapOf(AVVideoCodecKey to AVVideoCodecTypeJPEG))
            output.capturePhotoWithSettings(settings, captureDelegate)
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val session = AVCaptureSession().apply {
                sessionPreset = AVCaptureSessionPresetPhoto

                addInput(createInputDevice(lens))
                addOutput(output)
                startRunning()
            }

            captureSession = session

            val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
            }

            object : UIView(frame = CGRectZero.readValue()) {
                override fun layoutSubviews() {
                    super.layoutSubviews()
                    previewLayer.frame = bounds
                }
            }.apply {
                layer.addSublayer(previewLayer)
            }
        },
        onRelease = {
            captureSession?.stopRunning()
        }
    )
}

private class CaptureDelegate : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
    var onCapture: ((PhotoResult) -> Unit)? = null

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        if (error != null) throw RuntimeException(error.localizedDescription)

        val data = didFinishProcessingPhoto.fileDataRepresentation()?.toByteArray()
        if (data != null) onCapture?.invoke(PhotoResult(data))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun createInputDevice(lens: CameraLens): AVCaptureDeviceInput {
    val device = AVCaptureDevice.defaultDeviceWithDeviceType(
        deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
        mediaType = AVMediaTypeVideo,
        position = lens.toDevicePosition(),
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

private fun CameraLens.toDevicePosition() = when(this) {
    CameraLens.Front -> AVCaptureDevicePositionFront
    CameraLens.Back -> AVCaptureDevicePositionBack
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = memScoped {
    val buffer = ByteArray(length.toInt())
    buffer.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
    buffer
}

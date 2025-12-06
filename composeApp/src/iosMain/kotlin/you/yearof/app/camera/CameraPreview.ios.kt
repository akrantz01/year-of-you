package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView
import platform.UIKit.UIViewMeta

private const val CornerRadius: Double = 16.0

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    PreviewAspectRatio(modifier = modifier) { inner ->
        UIKitView(
            modifier = inner,
            factory = {
                PreviewView().apply {
                    clipsToBounds = true
                    layer.cornerRadius = CornerRadius

                    previewLayer.session = controller.camera.session
                    previewLayer.videoGravity = AVLayerVideoGravityResizeAspect
                    previewLayer.connection?.videoOrientation = AVCaptureVideoOrientationPortrait
                }
            },
        )
    }
}

private class PreviewView : UIView(frame = CGRectZero.readValue()) {
    val previewLayer: AVCaptureVideoPreviewLayer
        get() = layer as AVCaptureVideoPreviewLayer

    companion object : UIViewMeta() {
        override fun layerClass() = AVCaptureVideoPreviewLayer
    }
}

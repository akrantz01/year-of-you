package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    PreviewAspectRatio(modifier = modifier) { inner ->
        UIKitView(
            modifier = inner,
            factory = {
                val previewLayer =
                    AVCaptureVideoPreviewLayer(session = controller.camera.session).apply {
                        videoGravity = AVLayerVideoGravityResizeAspect
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
        )
    }
}

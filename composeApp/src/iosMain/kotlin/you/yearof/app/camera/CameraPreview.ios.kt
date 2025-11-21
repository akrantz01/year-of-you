package you.yearof.app.camera

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val previewAspectRatio = 3f / 4f
        val previewModifier =
            if (maxHeight * previewAspectRatio <= maxWidth) {
                Modifier.fillMaxHeight().aspectRatio(previewAspectRatio)
            } else {
                Modifier.fillMaxWidth().aspectRatio(previewAspectRatio)
            }

        UIKitView(
            modifier = previewModifier,
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

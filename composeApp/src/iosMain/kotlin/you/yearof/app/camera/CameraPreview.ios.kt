package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    UIKitView(
        modifier = modifier,
        factory = {
            val previewLayer =
                AVCaptureVideoPreviewLayer(session = controller.camera.session).apply {
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
    )
}

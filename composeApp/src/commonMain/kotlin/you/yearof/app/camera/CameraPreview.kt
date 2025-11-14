package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
)

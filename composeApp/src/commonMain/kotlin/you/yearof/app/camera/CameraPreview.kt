package you.yearof.app.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
)

private const val PreviewAspectRatio = 3f / 4f

@Composable
internal fun PreviewAspectRatio(
    modifier: Modifier = Modifier,
    composable: @Composable (modifier: Modifier) -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        composable(modifier.fillMaxWidth().aspectRatio(PreviewAspectRatio))
    }
}

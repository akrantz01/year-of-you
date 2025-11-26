package you.yearof.app.camera

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

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
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val aspectRatioModifier =
            if (maxHeight * PreviewAspectRatio <= maxWidth) {
                Modifier.fillMaxHeight().aspectRatio(PreviewAspectRatio)
            } else {
                Modifier.fillMaxWidth().aspectRatio(PreviewAspectRatio)
            }

        composable(aspectRatioModifier)
    }
}

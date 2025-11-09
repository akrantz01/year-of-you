package you.yearof.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class CameraLens {
    Front, Back;

    fun opposite() = when (this) {
        Front -> Back
        Back -> Front
    }
}

@Composable
expect fun CameraPreview(modifier: Modifier = Modifier, lens: CameraLens)

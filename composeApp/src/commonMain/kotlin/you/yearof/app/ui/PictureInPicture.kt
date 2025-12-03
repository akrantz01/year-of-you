package you.yearof.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.io.files.Path
import you.yearof.app.util.asOkioPath

@Composable
fun PictureInPicture(
    front: Path,
    back: Path,
    initiallySwapped: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var swapped by remember { mutableStateOf(initiallySwapped) }

    // TODO: migrate to kotlinx.io once supported by coil
    val frontModel = remember(front) { front.asOkioPath() }
    val backModel = remember(back) { back.asOkioPath() }

    val baseModel = if (swapped) frontModel else backModel
    val overlayModel = if (swapped) backModel else frontModel

    Box(modifier = modifier.fillMaxWidth().aspectRatio(3f / 4f)) {
        AsyncImage(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
            model = baseModel,
            contentDescription = null,
        )

        AsyncImage(
            modifier =
                Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.3f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(12.dp))
                    .clickable {
                        swapped = !swapped
                    },
            model = overlayModel,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

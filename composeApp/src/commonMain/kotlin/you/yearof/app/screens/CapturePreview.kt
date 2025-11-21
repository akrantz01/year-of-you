package you.yearof.app.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
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
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.arrow_left
import coil3.compose.AsyncImage
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.painterResource

@Composable
fun CapturePreview(
    frontPath: String,
    backPath: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        IconButton(
            onClick = onCancel,
            colors = iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(Res.drawable.arrow_left),
                contentDescription = "Back",
            )
        }

        PictureInPicture(
            modifier = Modifier.fillMaxWidth(),
            front = frontPath,
            back = backPath,
        )
    }
}

@Composable
fun PictureInPicture(
    front: String,
    back: String,
    modifier: Modifier = Modifier,
) {
    var swapped by remember { mutableStateOf(false) }

    val frontModel = remember(front) { front.toPath() }
    val backModel = remember(back) { back.toPath() }

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
                    .fillMaxWidth(0.4f)
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

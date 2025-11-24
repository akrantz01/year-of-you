package you.yearof.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.arrow_left
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.dto.CompletedCapture
import you.yearof.app.ui.PictureInPicture

@Composable
fun CapturePreviewScreen(
    capture: CompletedCapture,
    modifier: Modifier = Modifier,
    viewModel: CapturePreviewViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        IconButton(
            onClick = viewModel::onCancel,
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
            front = capture.frontPath,
            back = capture.backPath,
        )

        Button(onClick = { viewModel.onSave(capture) }) {
            Text("Save")
        }
    }
}

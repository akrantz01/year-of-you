package you.yearof.app.screens.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.arrow_left
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.dto.CompletedCapture
import you.yearof.app.ui.LoadingButton
import you.yearof.app.ui.PictureInPicture
import you.yearof.app.ui.Switch

@Composable
fun CapturePreviewScreen(
    capture: CompletedCapture,
    modifier: Modifier = Modifier,
    viewModel: CapturePreviewViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val authenticated by viewModel.authenticated.collectAsState()
    val containerScroll = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().verticalScroll(containerScroll)) {
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
            front = Path(capture.frontPath),
            back = Path(capture.backPath),
            initiallySwapped = capture.swapped,
        )

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.captionState,
                label = { Text("Caption") },
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 5),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
            )

            if (authenticated) {
                Switch(
                    label = "Share with friends?",
                    checked = uiState.share,
                    onChange = viewModel::onShareChange,
                )
            }

            val progress = uiState.uploadProgress
            if (uiState.loading && progress != null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progress },
                )
            }

            LoadingButton(
                modifier = Modifier.align(Alignment.End),
                text = "Save",
                onClick = { viewModel.onSave(capture) },
                loading = uiState.loading,
            )
        }
    }
}

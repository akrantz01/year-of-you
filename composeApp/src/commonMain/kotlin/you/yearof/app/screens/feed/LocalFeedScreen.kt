package you.yearof.app.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.globe
import app.composeapp.generated.resources.globe_wifi
import app.composeapp.generated.resources.mobile
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.database.Capture
import you.yearof.app.ui.BaseCaptureCard
import you.yearof.app.ui.ImageSource
import you.yearof.app.ui.StatusLine

@Composable
fun LocalFeedScreen(
    modifier: Modifier = Modifier,
    viewModel: LocalFeedViewModel = koinViewModel(),
) {
    val captures = viewModel.latestCaptures.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            count = captures.itemCount,
            key = captures.itemKey { it.id },
        ) { index ->
            val capture = captures[index]
            if (capture != null) {
                CaptureCard(capture = capture)
            } else {
                // TODO: show loading spinner/state
            }
        }
    }
}

@Composable
private fun CaptureCard(
    capture: Capture,
    modifier: Modifier = Modifier,
) {
    BaseCaptureCard(
        modifier = modifier,
        timestamp = capture.at,
        front = ImageSource.Local(capture.frontPath),
        back = ImageSource.Local(capture.backPath),
        swapped = capture.swapped,
        caption = capture.caption,
        statusLine = {
            if (capture.shared) {
                if (capture.uploadedAt != null) {
                    StatusLine(
                        icon = Res.drawable.globe,
                        iconDescription = "Globe icon",
                        description = "Shared",
                    )
                } else {
                    StatusLine(
                        icon = Res.drawable.globe_wifi,
                        iconDescription = "Globe with wi-fi icon",
                        description = "Waiting for network...",
                    )
                }
            } else {
                StatusLine(
                    icon = Res.drawable.mobile,
                    iconDescription = "Mobile phone",
                    description = "Local only",
                )
            }
        }
    )
}

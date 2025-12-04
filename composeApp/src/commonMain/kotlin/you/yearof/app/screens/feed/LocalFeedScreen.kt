package you.yearof.app.screens.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import you.yearof.app.resources.Res
import you.yearof.app.resources.globe
import you.yearof.app.resources.globe_wifi
import you.yearof.app.resources.mobile
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
    BaseCaptureFeed(
        modifier = modifier.fillMaxSize(),
        type = FeedType.Local,
        onSwitch = viewModel::toSharedFeed,
        captures = viewModel.latestCaptures,
        key = { it.id },
        item = { CaptureCard(capture = it) },
    )
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

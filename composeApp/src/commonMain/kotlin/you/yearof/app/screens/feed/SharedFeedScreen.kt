package you.yearof.app.screens.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import you.yearof.app.resources.Res
import you.yearof.app.resources.user_solid
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.database.RemoteCapture
import you.yearof.app.ui.BaseCaptureCard
import you.yearof.app.ui.ImageSource
import you.yearof.app.ui.StatusLine

@Composable
fun SharedFeedScreen(
    modifier: Modifier = Modifier,
    viewModel: SharedFeedViewModel = koinViewModel(),
) {
    BaseCaptureFeed(
        modifier = modifier.fillMaxSize(),
        type = FeedType.Shared,
        onSwitch = viewModel::toLocalFeed,
        captures = viewModel.feed,
        key = { it.id },
        item = { CaptureCard(capture = it) },
    )
}

@Composable
private fun CaptureCard(
    capture: RemoteCapture,
    modifier: Modifier = Modifier,
) {
    BaseCaptureCard(
        modifier = modifier,
        timestamp = capture.takenAt,
        front = ImageSource.Remote(capture.frontUrl),
        back = ImageSource.Remote(capture.backUrl),
        swapped = capture.swapped,
        statusLine = {
            StatusLine(
                icon = Res.drawable.user_solid,
                iconDescription = "Author",
                description = "By ${capture.accountUsername}",
            )
        }
    )
}

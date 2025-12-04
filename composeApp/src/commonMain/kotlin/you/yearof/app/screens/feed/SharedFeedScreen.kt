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
import app.composeapp.generated.resources.user_solid
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
    val captures = viewModel.feed.collectAsLazyPagingItems()

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
                // TODO: resolve to username
                description = "By user ${capture.accountId}",
            )
        }
    )
}

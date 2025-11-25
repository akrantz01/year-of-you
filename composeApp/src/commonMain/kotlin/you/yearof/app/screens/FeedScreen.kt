package you.yearof.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.ui.PictureInPicture

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel(),
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
                // TODO: attach extra metadata
                PictureInPicture(
                    front = capture.frontPath,
                    back = capture.backPath,
                    initiallySwapped = capture.swapped,
                )
            } else {
                // TODO: show loading spinner/state
            }
        }
    }
}

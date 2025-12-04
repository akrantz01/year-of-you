package you.yearof.app.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow

@Composable
fun <T : Any> BaseCaptureFeed(
    captures: Flow<PagingData<T>>,
    key: (T) -> Any,
    item: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val captures = captures.collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            count = captures.itemCount,
            key = captures.itemKey(key),
        ) { index ->
            val capture = captures[index]
            if (capture != null) {
                item(capture)
            } else {
                // TODO: show loading spinner/state
            }
        }
    }
}

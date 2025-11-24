package you.yearof.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.ui.PictureInPicture

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val captures by viewModel.latest.collectAsState(initial = emptyList())

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (capture in captures) {
            Row {
                // TODO: attach extra metadata
                PictureInPicture(front = capture.frontPath, back = capture.backPath)
            }
        }
    }
}

package you.yearof.app.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.database.Capture
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
                CaptureItem(capture = capture)
            } else {
                // TODO: show loading spinner/state
            }
        }
    }
}

val dateFormat =
    LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        day()
        chars(", ")
        year()
    }

val timeFormat =
    LocalTime.Format {
        amPmHour()
        char(':')
        minute()
        char(':')
        second()
        char(' ')
        amPmMarker("AM", "PM")
    }

@Composable
private fun CaptureItem(
    capture: Capture,
    modifier: Modifier = Modifier,
) {
    val timestamp = capture.at.toLocalDateTime(TimeZone.currentSystemDefault())

    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = timestamp.date.format(dateFormat),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = timestamp.time.format(timeFormat),
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                )
            }
            PictureInPicture(
                modifier = Modifier.padding(2.dp),
                front = capture.frontPath,
                back = capture.backPath,
                initiallySwapped = capture.swapped,
            )

            if (capture.caption.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = capture.caption,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

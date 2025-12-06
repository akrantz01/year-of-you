package you.yearof.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Instant

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
fun BaseCaptureCard(
    timestamp: Instant,
    front: ImageSource,
    back: ImageSource,
    swapped: Boolean,
    modifier: Modifier = Modifier,
    caption: String = "",
    statusLine: @Composable (() -> Unit)? = null,
) {
    val timestamp = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())

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

            statusLine?.invoke()

            PictureInPicture(
                modifier = Modifier.padding(2.dp),
                front = front,
                back = back,
                initiallySwapped = swapped,
            )

            if (caption.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = caption,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun StatusLine(
    icon: DrawableResource,
    iconDescription: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(icon),
            contentDescription = iconDescription,
        )
        Text(description, fontStyle = FontStyle.Italic)
    }
}

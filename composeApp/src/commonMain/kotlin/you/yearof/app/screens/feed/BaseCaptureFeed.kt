package you.yearof.app.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

enum class FeedType {
    Local, Shared
}

@Composable
fun <T : Any> BaseCaptureFeed(
    type: FeedType,
    onSwitch: () -> Unit,
    captures: Flow<PagingData<T>>,
    key: (T) -> Any,
    item: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val index = FeedType.entries.indexOf(type)
    val captures = captures.collectAsLazyPagingItems()

    FloatingTabBar(
        modifier = modifier.fillMaxSize(),
        options = FeedType.entries.map { it.name },
        selected = index,
        onSelect = { if (it != index) onSwitch() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
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
}

@Composable
fun FloatingTabBar(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    barShape: Shape = RoundedCornerShape(24.dp),
    tonalElevation: Dp = 6.dp,
    shadowElevation: Dp = 12.dp,
    bottomMargin: Dp = 16.dp,
    maxBarWidth: Dp = 360.dp,
    content: @Composable (PaddingValues) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val barPlaceables = subcompose("bar") {
            Box(
                Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(bottom = bottomMargin)
            ) {
                Surface(
                    shape = barShape,
                    tonalElevation = tonalElevation,
                    shadowElevation = shadowElevation
                ) {
                    Box(Modifier.widthIn(max = maxBarWidth)) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.padding(6.dp),
                        ) {
                            options.forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = index == selected,
                                    onClick = { onSelect(index) },
                                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        val barW = barPlaceables.maxOfOrNull { it.width } ?: 0
        val barH = barPlaceables.maxOfOrNull { it.height } ?: 0

        val contentPadding = PaddingValues(bottom = barH.toDp())
        val contentPlaceables = subcompose("content") { content(contentPadding) }.map { it.measure(constraints) }

        val layoutW = constraints.maxWidth
        val layoutH = constraints.maxHeight
        layout(layoutW, layoutH) {
            contentPlaceables.forEach { it.place(0, 0) }
            val x = ((layoutW - barW) / 2f).roundToInt()
            val y = layoutH - barH
            barPlaceables.forEach { it.place(x, y) }
        }
    }
}

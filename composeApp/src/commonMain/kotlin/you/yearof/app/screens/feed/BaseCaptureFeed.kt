package you.yearof.app.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import you.yearof.app.resources.Res
import you.yearof.app.resources.globe
import you.yearof.app.resources.mobile
import kotlin.math.roundToInt

enum class FeedType(val icon: DrawableResource) {
    Local(Res.drawable.mobile), Shared(Res.drawable.globe);
}

@Composable
fun <T : Any> BaseCaptureFeed(
    type: FeedType,
    onSwitch: () -> Unit,
    captures: Flow<PagingData<T>>,
    key: (T) -> Any,
    item: @Composable (T) -> Unit,
    showTabBar: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (showTabBar) {
        FloatingTabBar(
            modifier = modifier.fillMaxSize(),
            options = FeedType.entries,
            selected = type,
            onSelect = { if (it != type) onSwitch() },
        ) { padding ->
            CaptureFeedImpl(
                padding = padding,
                captures = captures,
                key = key,
                item = item,
            )
        }
    } else {
        CaptureFeedImpl(
            captures = captures,
            key = key,
            item = item,
        )
    }
}

@Composable
private fun <T : Any> CaptureFeedImpl(
    captures: Flow<PagingData<T>>,
    key: (T) -> Any,
    item: @Composable (T) -> Unit,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    val captures = captures.collectAsLazyPagingItems()

    val loadState = captures.loadState
    val isInitialLoading = loadState.refresh is LoadState.Loading && captures.itemCount == 0
    val isEmpty = loadState.refresh is LoadState.NotLoading &&
        loadState.append.endOfPaginationReached &&
        captures.itemCount == 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        when {
            isInitialLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            isEmpty -> {
                EmptyFeedState(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = padding,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        count = captures.itemCount,
                        key = captures.itemKey(key),
                    ) { index ->
                        captures[index]?.let { item(it) }
                    }

                    if (loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No captures yet",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FloatingTabBar(
    options: List<FeedType>,
    selected: FeedType,
    onSelect: (FeedType) -> Unit,
    modifier: Modifier = Modifier,
    barShape: Shape = RoundedCornerShape(24.dp),
    tonalElevation: Dp = 6.dp,
    shadowElevation: Dp = 8.dp,
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
                                    selected = label == selected,
                                    onClick = { onSelect(label) },
                                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                                    icon = {
                                        Icon(
                                            painter = painterResource(label.icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                        )
                                    },
                                ) {
                                    Text(
                                        text = label.name,
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

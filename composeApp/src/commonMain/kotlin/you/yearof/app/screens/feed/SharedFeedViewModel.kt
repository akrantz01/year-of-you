package you.yearof.app.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.api.CaptureService
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.FeedNav

@KoinViewModel
class SharedFeedViewModel(
    captureService: CaptureService,
    private val navigationCoordinator: NavigationCoordinator,
) : ViewModel() {
    private val pager = captureService.all()

    val feed = pager.flow.cachedIn(viewModelScope)

    fun toLocalFeed() = viewModelScope.launch {
        navigationCoordinator.go(FeedNav.LocalFeed)
    }
}

package you.yearof.app.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import you.yearof.app.api.UserService
import you.yearof.app.database.CaptureDao
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.FeedNav

@KoinViewModel
class LocalFeedViewModel(
    @Provided private val captures: CaptureDao,
    private val navigationCoordinator: NavigationCoordinator,
    userService: UserService,
) : ViewModel() {
    private val pager = Pager(PagingConfig(pageSize = 10, enablePlaceholders = false)) { captures.all() }
    val latestCaptures = pager.flow.cachedIn(viewModelScope)

    val authenticated = userService.authenticatedAsState(viewModelScope)

    fun toSharedFeed() = viewModelScope.launch {
        navigationCoordinator.navigateTo(FeedNav.SharedFeed)
    }
}

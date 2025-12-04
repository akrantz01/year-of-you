package you.yearof.app.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import you.yearof.app.database.CaptureDao

@KoinViewModel
class FeedViewModel(
    @Provided private val captures: CaptureDao,
) : ViewModel() {
    private val pager = Pager(PagingConfig(10)) { captures.all() }
    val latestCaptures = pager.flow.cachedIn(viewModelScope)
}

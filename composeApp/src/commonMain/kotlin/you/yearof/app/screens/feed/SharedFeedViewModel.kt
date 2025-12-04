package you.yearof.app.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.api.CaptureService

@KoinViewModel
class SharedFeedViewModel(
    captureService: CaptureService,
) : ViewModel() {
    private val pager = captureService.all()

    val feed = pager.flow.cachedIn(viewModelScope)
}

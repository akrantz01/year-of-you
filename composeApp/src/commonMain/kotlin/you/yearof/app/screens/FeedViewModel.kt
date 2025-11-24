package you.yearof.app.screens

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import you.yearof.app.database.CaptureDao

@KoinViewModel
class FeedViewModel(
    @Provided private val captures: CaptureDao,
) : ViewModel() {
    val latest = captures.all()
}

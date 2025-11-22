package you.yearof.app.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.dto.CompletedCapture
import you.yearof.app.navigation.NavigationCoordinator

@KoinViewModel
class CaptureViewModel(
    private val navigationCoordinator: NavigationCoordinator,
) : ViewModel() {
    fun onCaptureComplete(capture: CompletedCapture) {
        viewModelScope.launch {
            navigationCoordinator.navigateTo(
                Route.CapturePreview.from(capture),
                options = { launchSingleTop = true },
            )
        }
    }
}

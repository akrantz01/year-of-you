package you.yearof.app.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.ProfileNav

@KoinViewModel
class ProfileViewModel(
    private val navigationCoordinator: NavigationCoordinator,
) : ViewModel() {
    fun showProfile() = viewModelScope.launch {
        navigationCoordinator.navigateTo(ProfileNav.AccountLogin)
    }
}

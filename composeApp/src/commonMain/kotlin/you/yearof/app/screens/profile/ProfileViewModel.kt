package you.yearof.app.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import you.yearof.app.api.UserService
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.ProfileNav

@KoinViewModel
class ProfileViewModel(
    private val navigationCoordinator: NavigationCoordinator,
    private val userService: UserService,
) : ViewModel() {
    val authState = userService.state

    fun toLogin() = viewModelScope.launch {
        navigationCoordinator.navigateTo(ProfileNav.AccountLogin)
    }

    fun toSettings() = viewModelScope.launch {
        navigationCoordinator.navigateTo(ProfileNav.AccountSettings)
    }

    fun logout() = viewModelScope.launch {
        userService.logout()
    }
}

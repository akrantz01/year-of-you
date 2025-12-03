package you.yearof.app.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.core.annotation.Factory
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.ProfileNav
import you.yearof.app.screens.account.AccountRouter
import you.yearof.app.screens.account.RegisterScreen

@Composable
fun ProfileRegistrationScreen(
    modifier: Modifier = Modifier,
) {
    val router = koinInject<ProfileRegisterRouter>()
    RegisterScreen(modifier = modifier, router = router)
}

@Factory
class ProfileRegisterRouter(
    private val navigationCoordinator: NavigationCoordinator,
) : AccountRouter {
    override suspend fun onSuccess() {
        navigationCoordinator.navigateTo(ProfileNav.Profile) {
            popUpTo(ProfileNav.Profile) { inclusive = true }
        }
    }

    override suspend fun toOpposite() {
        navigationCoordinator.navigateTo(ProfileNav.AccountLogin)
    }

    override suspend fun onCancel() {
        navigationCoordinator.navigateTo(ProfileNav.Profile) {
            popUpTo(ProfileNav.Profile) { inclusive = true }
        }
    }
}

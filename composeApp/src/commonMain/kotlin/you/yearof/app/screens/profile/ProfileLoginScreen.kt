package you.yearof.app.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.core.annotation.Factory
import you.yearof.app.navigation.NavigationCoordinator
import you.yearof.app.screens.ProfileNav
import you.yearof.app.screens.account.AccountRouter
import you.yearof.app.screens.account.LoginScreen

@Composable
fun ProfileLoginScreen(
    modifier: Modifier = Modifier,
) {
    val router = koinInject<ProfileLoginRouter>()
    LoginScreen(modifier = modifier, router = router)
}

@Factory
class ProfileLoginRouter(
    private val navigationCoordinator: NavigationCoordinator,
) : AccountRouter {
    override suspend fun onSuccess() {
        navigationCoordinator.navigateTo(ProfileNav.Profile) {
            popUpTo(ProfileNav.Profile) { inclusive = true }
        }
    }

    override suspend fun toOpposite() {
        navigationCoordinator.navigateTo(ProfileNav.AccountRegister)
    }

    override suspend fun onCancel() {
        navigationCoordinator.navigateTo(ProfileNav.Profile) {
            popUpTo(ProfileNav.Profile) { inclusive = true }
        }
    }
}

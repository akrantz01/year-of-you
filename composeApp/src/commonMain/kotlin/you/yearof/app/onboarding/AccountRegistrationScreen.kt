package you.yearof.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.screens.account.RegisterScreen

@Composable
fun AccountRegistrationScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val router = remember(viewModel) { viewModel.registrationAccountRouter() }
    RegisterScreen(modifier = modifier, router = router)
}

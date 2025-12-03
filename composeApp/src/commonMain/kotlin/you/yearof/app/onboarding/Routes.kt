package you.yearof.app.onboarding

import kotlinx.serialization.Serializable
import you.yearof.app.navigation.NavigationRoute

@Serializable
data object Onboarding : NavigationRoute

@Serializable
sealed interface OnboardingRoute : NavigationRoute {
    @Serializable
    data object Camera : OnboardingRoute

    @Serializable
    data object Notifications : OnboardingRoute

    @Serializable
    data object AccountPrompt : OnboardingRoute

    @Serializable
    data object AccountRegistration : OnboardingRoute

    @Serializable
    data object AccountConfirmation : OnboardingRoute

    @Serializable
    data object AccountLogin : OnboardingRoute
}

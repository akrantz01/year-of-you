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
}

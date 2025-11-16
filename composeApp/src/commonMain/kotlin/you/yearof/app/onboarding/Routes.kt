package you.yearof.app.onboarding

import kotlinx.serialization.Serializable

@Serializable
data object Onboarding

@Serializable
sealed interface OnboardingRoute {
    @Serializable
    data object Camera : OnboardingRoute

    @Serializable
    data object Notifications : OnboardingRoute
}

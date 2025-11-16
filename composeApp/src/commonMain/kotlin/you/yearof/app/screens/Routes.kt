package you.yearof.app.screens

import kotlinx.serialization.Serializable

@Serializable
data object Main

@Serializable
sealed interface Route {
    @Serializable
    data object Feed : Route

    @Serializable
    data object Capture : Route

    @Serializable
    data object Profile : Route
}

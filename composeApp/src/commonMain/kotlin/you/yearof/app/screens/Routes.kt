package you.yearof.app.screens

import kotlinx.serialization.Serializable
import you.yearof.app.dto.CompletedCapture
import you.yearof.app.navigation.NavigationRoute
import kotlin.time.Instant

/**
 * Top-level graph for the main portion of the app (after onboarding/initialization).
 */
@Serializable
data object MainGraph : NavigationRoute

/**
 * Marker objects for tab graphs.
 */
@Serializable
data object FeedGraph : NavigationRoute

@Serializable
data object CaptureGraph : NavigationRoute

@Serializable
data object ProfileGraph : NavigationRoute

@Serializable
sealed interface FeedNav : NavigationRoute {
    @Serializable
    data object Feed : FeedNav
}

@Serializable
sealed interface CaptureNav : NavigationRoute {
    @Serializable
    data object Capture : CaptureNav

    @Serializable
    data class CapturePreview(
        val frontPath: String,
        val backPath: String,
        val swapped: Boolean,
        val timestamp: Long,
    ) : CaptureNav {
        fun toCompletedCapture(): CompletedCapture =
            CompletedCapture(
                frontPath = frontPath,
                backPath = backPath,
                swapped = swapped,
                timestamp = Instant.fromEpochMilliseconds(timestamp),
            )

        companion object {
            fun from(completed: CompletedCapture): CapturePreview =
                CapturePreview(
                    frontPath = completed.frontPath,
                    backPath = completed.backPath,
                    swapped = completed.swapped,
                    timestamp = completed.timestamp.toEpochMilliseconds(),
                )
        }
    }
}

@Serializable
sealed interface ProfileNav : NavigationRoute {
    @Serializable
    data object Profile : ProfileNav

    @Serializable
    data object AccountLogin : ProfileNav
}

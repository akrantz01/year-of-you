package you.yearof.app.screens

import kotlinx.serialization.Serializable
import you.yearof.app.dto.CompletedCapture
import kotlin.time.Instant

@Serializable
data object Main

@Serializable
sealed interface Route {
    @Serializable
    data object Feed : Route

    @Serializable
    data object Capture : Route

    @Serializable
    data class CapturePreview(
        val frontPath: String,
        val backPath: String,
        val timestamp: Long,
    ) : Route {
        fun toCompletedCapture(): CompletedCapture =
            CompletedCapture(
                frontPath = frontPath,
                backPath = backPath,
                timestamp = Instant.fromEpochMilliseconds(timestamp),
            )

        companion object {
            fun from(completed: CompletedCapture): CapturePreview =
                CapturePreview(
                    frontPath = completed.frontPath,
                    backPath = completed.backPath,
                    timestamp = completed.timestamp.toEpochMilliseconds(),
                )
        }
    }

    @Serializable
    data object Profile : Route
}

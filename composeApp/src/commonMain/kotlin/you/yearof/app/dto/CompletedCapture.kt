package you.yearof.app.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.InstantComponentSerializer
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class CompletedCapture(
    val frontPath: String,
    val backPath: String,
    @Serializable(with = InstantComponentSerializer::class) val timestamp: Instant = Clock.System.now(),
)

package you.yearof.app.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.InstantComponentSerializer
import you.yearof.app.camera.CameraPosition
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class CompletedCapture(
    val frontPath: String,
    val backPath: String,
    val swapped: Boolean,
    @Serializable(with = InstantComponentSerializer::class) val timestamp: Instant = Clock.System.now(),
)

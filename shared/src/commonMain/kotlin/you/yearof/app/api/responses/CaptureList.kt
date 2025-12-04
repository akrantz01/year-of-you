package you.yearof.app.api.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CaptureList(
    @SerialName("account_id")
    val accountId: UInt,
    val front: String,
    val back: String,
    val swapped: Boolean,
    @SerialName("taken_at")
    val takenAt: Instant,
    @SerialName("uploaded_at")
    val uploadedAt: Instant,
)

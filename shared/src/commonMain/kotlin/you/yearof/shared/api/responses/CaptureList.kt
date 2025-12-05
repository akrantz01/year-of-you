package you.yearof.shared.api.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CapturePage(
    val items: List<CaptureListItem>,
    @SerialName("next_cursor")
    val nextCursor: String? = null,
)

@Serializable
data class CaptureListItem(
    val id: UInt,
    val account: AccountListItem,
    val front: String,
    val back: String,
    val swapped: Boolean,
    @SerialName("taken_at")
    val takenAt: Instant,
    @SerialName("uploaded_at")
    val uploadedAt: Instant,
)

@Serializable
data class AccountListItem(
    val id: UInt,
    val username: String,
)

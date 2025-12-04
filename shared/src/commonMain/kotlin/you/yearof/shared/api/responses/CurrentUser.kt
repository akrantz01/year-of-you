package you.yearof.shared.api.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentUser(
    val id: UInt,
    @SerialName("display_name")
    val displayName: String,
    val username: String,
)

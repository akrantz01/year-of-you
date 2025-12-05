package you.yearof.shared.api.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRequest(
    @SerialName("display_name")
    val displayName: String? = null,
    val username: String? = null,
)

package you.yearof.app.api.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("display_name")
    val displayName: String,
    val username: String,
    val password: String,
)

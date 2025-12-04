package you.yearof.shared.api.requests

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val token: String,
)

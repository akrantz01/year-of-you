package you.yearof.app.api.requests

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val token: String,
)

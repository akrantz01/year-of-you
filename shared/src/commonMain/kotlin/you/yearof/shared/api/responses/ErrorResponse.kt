package you.yearof.shared.api.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val details: ErrorDetails? = null,
)

@Serializable
sealed interface ErrorDetails

@Serializable
@SerialName("validation")
data class ValidationErrorDetails(
    val errors: Map<String, String>,
) : ErrorDetails

@Serializable
@SerialName("not_found")
data class NotFoundErrorDetails(
    val resource: String,
    val identifier: String? = null,
) : ErrorDetails

@Serializable
@SerialName("conflict")
data class ConflictErrorDetails(
    val resource: String,
    val field: String? = null,
) : ErrorDetails

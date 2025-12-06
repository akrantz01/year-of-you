package you.yearof.server.exceptions

import io.ktor.http.HttpStatusCode
import you.yearof.shared.api.responses.ConflictErrorDetails
import you.yearof.shared.api.responses.ErrorDetails
import you.yearof.shared.api.responses.NotFoundErrorDetails

sealed class StructuredHttpException(
    override val message: String,
    val statusCode: HttpStatusCode,
    val details: ErrorDetails? = null,
) : Exception(message)

class BadRequestException(
    message: String,
    details: ErrorDetails? = null,
) : StructuredHttpException(message, HttpStatusCode.BadRequest, details)

class UnauthorizedException(
    message: String = "authentication required",
) : StructuredHttpException(message, HttpStatusCode.Unauthorized)

class ForbiddenException(
    message: String = "forbidden",
) : StructuredHttpException(message, HttpStatusCode.Forbidden)

class NotFoundException(
    message: String,
    details: NotFoundErrorDetails? = null,
) : StructuredHttpException(message, HttpStatusCode.NotFound, details)

class ConflictException(
    message: String,
    details: ConflictErrorDetails? = null,
) : StructuredHttpException(message, HttpStatusCode.Conflict, details)

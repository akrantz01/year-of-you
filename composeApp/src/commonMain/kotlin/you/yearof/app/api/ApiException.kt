package you.yearof.app.api

import io.ktor.http.*
import you.yearof.shared.api.responses.*

sealed class ApiException(
    message: String,
    val statusCode: HttpStatusCode,
    val errorResponse: ErrorResponse?,
) : Exception(message) {
    val details: ErrorDetails?
        get() = errorResponse?.details
}

open class ClientErrorException(
    statusCode: HttpStatusCode,
    errorResponse: ErrorResponse,
) : ApiException(errorResponse.message, statusCode, errorResponse)

class BadRequestException(
    errorResponse: ErrorResponse,
) : ClientErrorException(HttpStatusCode.BadRequest, errorResponse)

class UnauthorizedException(
    errorResponse: ErrorResponse,
) : ClientErrorException(HttpStatusCode.Unauthorized, errorResponse)

class ForbiddenException(
    errorResponse: ErrorResponse,
) : ClientErrorException(HttpStatusCode.Forbidden, errorResponse)

class NotFoundException(
    errorResponse: ErrorResponse,
) : ClientErrorException(HttpStatusCode.NotFound, errorResponse)

class ConflictException(
    errorResponse: ErrorResponse,
) : ClientErrorException(HttpStatusCode.Conflict, errorResponse)

class ServerErrorException(
    errorResponse: ErrorResponse,
) : ApiException(errorResponse.message, HttpStatusCode.InternalServerError, errorResponse)

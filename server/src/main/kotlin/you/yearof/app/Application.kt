package you.yearof.app

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.uuid.Uuid
import you.yearof.app.api.responses.ErrorResponse
import you.yearof.app.api.responses.ValidationErrorDetails
import you.yearof.app.exceptions.StructuredHttpException

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

fun Application.module() {
    install(CallId) {
        generate { Uuid.random().toString() }
        replyToHeader(HttpHeaders.XRequestId)
    }
    install(CallLogging) {
        callIdMdc("call-id")
    }

    install(ContentNegotiation) {
        json()
    }

    install(Resources)

    install(StatusPages) {
        exception<StructuredHttpException> { call, exception ->
            call.respond(
                status = exception.statusCode,
                message = ErrorResponse(
                    message = exception.message,
                    details = exception.details,
                )
            )
        }

        exception<BadRequestException> { call, exception ->
            val message = when (val cause = exception.cause) {
                is JsonConvertException -> cause.message?.lines()?.firstOrNull()
                else -> null
            }

            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(message ?: "bad request"),
            )
        }

        // TODO: provide better request validation exceptions
        exception<RequestValidationException> { call, exception ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    message = "invalid request body",
                    details = ValidationErrorDetails(
                        errors = exception.reasons.mapIndexed { index, reason ->
                            "error_$index" to reason
                        }.toMap()
                    ),
                )
            )
        }

        exception<UnsupportedMediaTypeException> { call, _ ->
            call.respond(HttpStatusCode.UnsupportedMediaType)
        }

        exception<Throwable> { call, exception ->
            call.application.environment.log.error("unhandled exception", exception)

            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(message = "an internal error occurred")
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}

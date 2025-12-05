package you.yearof.server.routes.authentication

import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import you.yearof.shared.api.Routes
import you.yearof.shared.api.requests.RegisterRequest
import you.yearof.server.database.tables.DisplayNameMaxLength
import you.yearof.server.database.tables.UsernameMaxLength
import you.yearof.server.database.tables.UsernameRegex
import you.yearof.server.exceptions.ConflictException
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.services.PasswordService
import you.yearof.server.services.TokenService
import you.yearof.server.util.isUniqueConstraintViolation
import you.yearof.shared.api.responses.LoginSuccess

internal fun Route.registerRoute(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    install(RequestValidation) {
        validate<RegisterRequest> { request ->
            when {
                request.displayName.isBlank() -> {
                    ValidationResult.Invalid("a display name is required")
                }

                request.displayName.length > DisplayNameMaxLength -> {
                    ValidationResult.Invalid(
                        "display name must $DisplayNameMaxLength characters or less",
                    )
                }

                request.username.isBlank() -> {
                    ValidationResult.Invalid("a username is required")
                }

                request.username.length > UsernameMaxLength -> {
                    ValidationResult.Invalid("username must be $UsernameMaxLength characters or less")
                }

                !request.username.matches(UsernameRegex) -> {
                    ValidationResult.Invalid(
                        "username can only consist of lowercase alphanumeric characters and underscore",
                    )
                }

                request.password.isBlank() -> {
                    ValidationResult.Invalid("a password is required")
                }

                else -> {
                    ValidationResult.Valid
                }
            }
        }
    }

    post<Routes.Register> {
        val requested = call.receive<RegisterRequest>()
        val account = try {
            accounts.create(
                displayName = requested.displayName,
                username = requested.username,
                password = PasswordService.hash(requested.password),
            )
        } catch (e: ExposedSQLException) {
            if (e.isUniqueConstraintViolation) throw ConflictException("username already in use")
            else throw e
        }

        // TODO: confirm account via email or something
        val token = tokens.issue(account)
        call.respond(LoginSuccess(accessToken = token.accessToken, refreshToken = token.refreshToken))
    }
}

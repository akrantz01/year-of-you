package you.yearof.server.routes.authentication

import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import you.yearof.app.api.Routes
import you.yearof.app.api.requests.RegisterRequest
import you.yearof.server.database.tables.DisplayNameMaxLength
import you.yearof.server.database.tables.UsernameMaxLength
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.services.PasswordService

private val UsernameRegex = Regex("^[a-z0-9_]+$")

@Serializable
data class RegisterResponse(
    val id: UInt,
    @SerialName("display_name")
    val displayName: String,
    val username: String,
)

internal fun Route.registerRoute(accounts: AccountRepository) {
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
        val account =
            accounts.create(
                displayName = requested.displayName,
                username = requested.username,
                password = PasswordService.hash(requested.password),
            )

        // TODO: generate valid token after registration
        call.respond(
            RegisterResponse(
                id = account.id.value,
                displayName = account.displayName,
                username = account.username,
            ),
        )
    }
}

package you.yearof.server.routes.accounts

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.resources.patch
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import you.yearof.server.database.tables.DisplayNameMaxLength
import you.yearof.server.database.tables.UsernameMaxLength
import you.yearof.server.database.tables.UsernameRegex
import you.yearof.server.exceptions.ConflictException
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.util.isUniqueConstraintViolation
import you.yearof.shared.api.Routes
import you.yearof.shared.api.requests.UpdateRequest
import you.yearof.shared.api.responses.CurrentUser

internal fun Route.updateRoute(accounts: AccountRepository) {
    install(RequestValidation) {
        validate<UpdateRequest> { request ->
            val issues = mutableListOf<String>()

            request.displayName?.let { displayName ->
                if (displayName.isBlank()) {
                    issues.add("a display name is required")
                } else if (displayName.length > UsernameMaxLength) {
                    issues.add("display name must be $DisplayNameMaxLength characters or less")
                }
            }

            request.username?.let { username ->
                if (username.isBlank()) {
                    issues.add("a username is required")
                } else if (username.length > UsernameMaxLength) {
                    issues.add("a username must be $UsernameMaxLength characters or less")
                } else if (!username.matches(UsernameRegex)) {
                    issues.add("username can only consist of lowercase alphanumeric characters and underscore")
                }
            }

            if (issues.isEmpty()) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid(issues)
            }
        }
    }

    patch<Routes.CurrentUser> {
        val account =
            call
                .principal<JWTPrincipal>()
                ?.subject
                ?.toUInt()
                ?.let { id -> accounts.get(id) }
        checkNotNull(account) // TODO: handle property

        val requested = call.receive<UpdateRequest>()
        requested.displayName?.let { displayName -> account.displayName = displayName }
        requested.username?.let { username -> account.username = username }

        try {
            accounts.update(account)
        } catch (e: ExposedSQLException) {
            if (e.isUniqueConstraintViolation) {
                throw ConflictException("username already in use")
            } else {
                throw e
            }
        }

        call.respond(
            CurrentUser(
                id = account.id.value,
                displayName = account.displayName,
                username = account.username,
            ),
        )
    }
}

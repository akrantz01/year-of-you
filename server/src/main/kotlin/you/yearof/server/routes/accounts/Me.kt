package you.yearof.server.routes.accounts

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import you.yearof.server.repositories.AccountRepository
import you.yearof.shared.api.Routes
import you.yearof.shared.api.responses.CurrentUser

fun Route.meRoute(accounts: AccountRepository) {
    get<Routes.CurrentUser> {
        val account =
            call
                .principal<JWTPrincipal>()
                ?.subject
                ?.toUInt()
                ?.let { id -> accounts.get(id) }
        checkNotNull(account) // TODO: handle properly

        call.respond(
            CurrentUser(
                id = account.id.value,
                displayName = account.displayName,
                username = account.username,
            ),
        )
    }
}

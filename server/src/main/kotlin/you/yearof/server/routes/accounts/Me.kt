package you.yearof.server.routes.accounts

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import you.yearof.shared.api.Routes
import you.yearof.shared.api.responses.CurrentUser
import you.yearof.server.repositories.AccountRepository

fun Route.meRoute(accounts: AccountRepository) {
    get<Routes.CurrentUser> {
        val principal = call.principal<JWTPrincipal>()!!
        val account = accounts.get(principal.subject!!.toUInt())
        checkNotNull(account)

        call.respond(CurrentUser(
            id = account.id.value,
            displayName = account.displayName,
            username = account.username,
        ))
    }
}

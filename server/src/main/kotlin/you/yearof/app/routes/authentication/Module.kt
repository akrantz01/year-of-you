package you.yearof.app.routes.authentication

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.services.TokenService

fun Application.authenticationRoutes(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    routing {
        registerRoute(accounts)
        loginRoute(accounts, tokens)
        refreshRoute(accounts, tokens)
    }
}

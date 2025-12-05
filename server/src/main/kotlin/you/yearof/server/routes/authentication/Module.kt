package you.yearof.server.routes.authentication

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.services.TokenService

fun Application.authenticationRoutes(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    routing {
        registerRoute(accounts, tokens)
        loginRoute(accounts, tokens)
        refreshRoute(accounts, tokens)
    }
}

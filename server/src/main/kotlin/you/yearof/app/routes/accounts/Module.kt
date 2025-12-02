package you.yearof.app.routes.accounts

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import you.yearof.app.repositories.AccountRepository

fun Application.accountsRoutes(accounts: AccountRepository) {
    routing {
        authenticate {
            meRoute(accounts)
        }
    }
}

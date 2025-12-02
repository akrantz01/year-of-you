package you.yearof.app.routes.accounts

import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import you.yearof.app.repositories.AccountRepository

@Resource("/accounts")
class Accounts {
    @Resource("/me")
    class Me(val parent: Accounts = Accounts())
}

fun Application.accountsRoutes(accounts: AccountRepository) {
    routing {
        authenticate {
            meRoute(accounts)
        }
    }
}

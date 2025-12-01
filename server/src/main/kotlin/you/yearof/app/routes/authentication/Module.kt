package you.yearof.app.routes.authentication

import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.services.TokenService

@Resource("/auth")
class Authentication {
    @Resource("/register")
    class Register(
        val parent: Authentication = Authentication(),
    )

    @Resource("/login")
    class Login(
        val parent: Authentication = Authentication(),
    )
}

fun Application.authenticationRoutes(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    routing {
        registerRoute(accounts)
        loginRoute(accounts, tokens)
    }
}

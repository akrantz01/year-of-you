package you.yearof.app.routes.authentication

import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.routing.routing
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.services.TokenService
import you.yearof.app.services.TokenUsage
import io.ktor.server.auth.Authentication as AuthenticationPlugin

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
    install(AuthenticationPlugin) {
        jwt {
            realm = "Year of You: access token"
            verifier(tokens.verifier)

            validate { credential ->
                tokens.validate(TokenUsage.Access, credential)
            }
        }

        jwt("refresh") {
            realm = "Year of You: refresh token"
            verifier(tokens.verifier)
            validate { credential ->
                tokens.validate(TokenUsage.Refresh, credential)
            }
        }
    }

    routing {
        registerRoute(accounts)
        loginRoute(accounts, tokens)
    }
}

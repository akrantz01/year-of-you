package you.yearof.app.routes.authentication

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.routing.routing
import you.yearof.app.api.DefaultRealm
import you.yearof.app.api.RefreshRealm
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.services.TokenService
import you.yearof.app.services.TokenUsage
import io.ktor.server.auth.Authentication as AuthenticationPlugin

fun Application.authenticationRoutes(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    install(AuthenticationPlugin) {
        jwt {
            realm = DefaultRealm
            verifier(tokens.verifier)

            validate { credential ->
                tokens.validate(TokenUsage.Access, credential)
            }
        }

        jwt("refresh") {
            realm = RefreshRealm
            verifier(tokens.verifier)
            validate { credential ->
                tokens.validate(TokenUsage.Refresh, credential)
            }
        }
    }

    routing {
        registerRoute(accounts)
        loginRoute(accounts, tokens)
        refreshRoute(accounts, tokens)
    }
}

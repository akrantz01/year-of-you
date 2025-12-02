package you.yearof.app.routes.authentication

import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import you.yearof.app.api.Routes
import you.yearof.app.api.requests.LoginRequest
import you.yearof.app.api.responses.LoginSuccess
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.services.PasswordService
import you.yearof.app.services.TokenService

internal fun Route.loginRoute(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    post<Routes.Login> {
        val request = call.receive<LoginRequest>()
        val account = accounts.findByUsername(request.username)
        checkNotNull(account) // TODO: handle error properly

        val result = PasswordService.verify(request.password, account.password)
        check(result.ok) // TODO: handle error properly

        val token = tokens.issue(account)
        call.respond(LoginSuccess(accessToken = token.accessToken, refreshToken = token.refreshToken))
    }
}

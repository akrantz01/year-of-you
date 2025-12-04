package you.yearof.server.routes.authentication

import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.delay
import you.yearof.shared.api.Routes
import you.yearof.shared.api.requests.LoginRequest
import you.yearof.shared.api.responses.LoginSuccess
import you.yearof.server.exceptions.UnauthorizedException
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.services.PasswordService
import you.yearof.server.services.TokenService
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

internal fun Route.loginRoute(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    post<Routes.Login> {
        val request = call.receive<LoginRequest>()
        val account = accounts.findByUsername(request.username)
        if (account == null) {
            // simulate password hashing
            // TODO: use dummy hash comparison
            delay(Random.nextInt(40..60).milliseconds)
            throw UnauthorizedException("invalid username or password")
        }

        val result = PasswordService.verify(request.password, account.password)
        if (!result.ok) {
            throw UnauthorizedException("invalid username or password")
        }
        // TODO: handle password hash upgrade

        val token = tokens.issue(account)
        call.respond(LoginSuccess(accessToken = token.accessToken, refreshToken = token.refreshToken))
    }
}

package you.yearof.app.routes.authentication

import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import you.yearof.app.api.Routes
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.services.PasswordService
import you.yearof.app.services.TokenService

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
)

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
        call.respond(LoginResponse(accessToken = token.accessToken, refreshToken = token.refreshToken))
    }
}

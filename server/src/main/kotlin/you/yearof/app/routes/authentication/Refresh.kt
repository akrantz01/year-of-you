package you.yearof.app.routes.authentication

import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import you.yearof.app.api.Routes
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.services.TokenService

@Serializable
data class RefreshResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String?,
)

internal fun Route.refreshRoute(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    authenticate("refresh") {
        post<Routes.Refresh> {
            val principal = call.principal<JWTPrincipal>()!!
            val account = accounts.get(principal.subject!!.toUInt())
            checkNotNull(account) // TODO: handle properly

            val refreshed = tokens.refresh(account, principal)
            call.respond(RefreshResponse(accessToken = refreshed.accessToken, refreshToken = refreshed.refreshToken))
        }
    }
}

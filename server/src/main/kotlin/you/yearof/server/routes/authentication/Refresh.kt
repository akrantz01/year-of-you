package you.yearof.server.routes.authentication

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import you.yearof.app.api.Routes
import you.yearof.app.api.requests.RefreshRequest
import you.yearof.app.api.responses.RefreshSuccess
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.services.TokenService

internal fun Route.refreshRoute(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    post<Routes.Refresh> {
        val request = call.receive<RefreshRequest>()
        val principal = tokens.verifyRefresh(request.token) ?: return@post call.respond(HttpStatusCode.Unauthorized)

        val account = accounts.get(principal.subject!!.toUInt())
        checkNotNull(account) // TODO: handle properly

        val refreshed = tokens.refresh(account, principal)
        call.respond(RefreshSuccess(accessToken = refreshed.accessToken, refreshToken = refreshed.refreshToken))
    }
}

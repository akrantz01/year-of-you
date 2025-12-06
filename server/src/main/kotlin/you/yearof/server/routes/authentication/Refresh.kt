package you.yearof.server.routes.authentication

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.services.TokenService
import you.yearof.shared.api.Routes
import you.yearof.shared.api.requests.RefreshRequest
import you.yearof.shared.api.responses.RefreshSuccess

internal fun Route.refreshRoute(
    accounts: AccountRepository,
    tokens: TokenService,
) {
    post<Routes.Refresh> {
        val request = call.receive<RefreshRequest>()
        val principal = tokens.verifyRefresh(request.token) ?: return@post call.respond(HttpStatusCode.Unauthorized)

        val account = principal.subject?.toUInt()?.let { id -> accounts.get(id) }
        checkNotNull(account) // TODO: handle properly

        val refreshed = tokens.refresh(account, principal)
        call.respond(RefreshSuccess(accessToken = refreshed.accessToken, refreshToken = refreshed.refreshToken))
    }
}

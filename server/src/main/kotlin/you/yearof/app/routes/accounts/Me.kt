package you.yearof.app.routes.accounts

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import you.yearof.app.api.Routes
import you.yearof.app.repositories.AccountRepository

@Serializable
data class MeResponse(
    val id: UInt,
    @SerialName("display_name")
    val displayName: String,
    val username: String,
)

fun Route.meRoute(accounts: AccountRepository) {
    get<Routes.CurrentUser> {
        val principal = call.principal<JWTPrincipal>()!!
        val account = accounts.get(principal.subject!!.toUInt())
        checkNotNull(account)

        call.respond(MeResponse(
            id = account.id.value,
            displayName = account.displayName,
            username = account.username,
        ))
    }
}

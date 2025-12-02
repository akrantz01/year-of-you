package you.yearof.app.routes.captures

import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.repositories.CaptureRepository

@Resource("/captures")
class Captures

fun Application.capturesRoutes(
    accounts: AccountRepository,
    captures: CaptureRepository,
) {
    routing {
        authenticate {
            uploadRoute(accounts, captures)
        }
    }
}

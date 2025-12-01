package you.yearof.app.routes.authentication

import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

@Resource("/auth")
class Authentication {
    @Resource("/register")
    class Register(
        val parent: Authentication = Authentication(),
    )
}

fun Application.authenticationRoutes() {
    routing {
        registerRoute()
    }
}

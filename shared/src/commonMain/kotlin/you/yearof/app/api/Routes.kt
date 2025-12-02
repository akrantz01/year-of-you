package you.yearof.app.api

import io.ktor.resources.Resource

object Routes {
    @Resource("/accounts/me")
    object CurrentUser

    @Resource("/auth/register")
    object Register

    @Resource("/auth/login")
    object Login

    @Resource("/auth/refresh")
    object Refresh

    @Resource("/captures")
    object Captures
}

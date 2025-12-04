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
    class Captures(
        // can't use UInts since passing a negative will be interpreted as positive
        val limit: Int = 10,
        val offset: Int = 0,
    )
}

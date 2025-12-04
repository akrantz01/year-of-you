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
    class Captures {
        @Resource("")
        class List(
            // can't use UInts since passing a negative will be interpreted as positive
            val limit: Int = 20,
            val cursor: String? = null,
            val parent: Captures = Captures(),
        )

        @Resource("{id}")
        class Id(val parent: Captures = Captures(), val id: UInt) {
            @Resource("front")
            class Front(val parent: Id) {
                companion object {
                    fun make(id: UInt) = Front(parent = Id(id = id))
                }
            }

            @Resource("back")
            class Back(val parent: Id) {
                companion object {
                    fun make(id: UInt) = Back(parent = Id(id = id))
                }
            }
        }
    }
}

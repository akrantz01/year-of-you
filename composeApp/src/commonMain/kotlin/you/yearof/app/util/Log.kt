package you.yearof.app.util

expect object Log {
    fun debug(
        tag: String,
        message: String,
    )

    fun info(
        tag: String,
        message: String,
    )

    fun warn(
        tag: String,
        message: String,
    )

    fun error(
        tag: String,
        message: String,
    )
}

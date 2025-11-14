package you.yearof.app.util

import platform.Foundation.NSLog

actual object Log {
    actual fun debug(
        tag: String,
        message: String,
    ) = log("DEBUG", tag, message)

    actual fun info(
        tag: String,
        message: String,
    ) = log("INFO", tag, message)

    actual fun warn(
        tag: String,
        message: String,
    ) = log("WARN", tag, message)

    actual fun error(
        tag: String,
        message: String,
    ) = log("ERROR", tag, message)

    private fun log(
        level: String,
        tag: String,
        message: String,
    ) {
        NSLog("$level <<<< $tag >>>> $message")
    }
}

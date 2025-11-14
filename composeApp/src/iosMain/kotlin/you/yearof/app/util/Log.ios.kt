package you.yearof.app.util

import platform.Foundation.NSLog

actual object Log {
    actual fun debug(
        tag: String,
        message: String,
    ) = write("DEBUG", tag, message)

    actual fun info(
        tag: String,
        message: String,
    ) = write("INFO", tag, message)

    actual fun warn(
        tag: String,
        message: String,
    ) = write("WARN", tag, message)

    actual fun error(
        tag: String,
        message: String,
    ) = write("ERROR", tag, message)

    private fun write(
        level: String,
        tag: String,
        message: String,
    ) {
        NSLog("$level <<<< $tag >>>> $message")
    }
}

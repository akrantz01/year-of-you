package you.yearof.app.util

import android.util.Log as AndroidLog

actual object Log {
    actual fun debug(
        tag: String,
        message: String,
    ) {
        AndroidLog.d(tag, message)
    }

    actual fun info(
        tag: String,
        message: String,
    ) {
        AndroidLog.i(tag, message)
    }

    actual fun warn(
        tag: String,
        message: String,
    ) {
        AndroidLog.w(tag, message)
    }

    actual fun error(
        tag: String,
        message: String,
    ) {
        AndroidLog.e(tag, message)
    }
}

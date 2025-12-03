package you.yearof.app.util

inline fun <T> Boolean.then(block: () -> T): T? = if (this) block() else null

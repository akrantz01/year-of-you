package you.yearof.app.util

import android.content.Context

class AndroidPaths(
    val context: Context,
) : Paths {
    override fun forDatabase(name: String): String = context.getDatabasePath(name).absolutePath
}

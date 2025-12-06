@file:Suppress("ktlint:standard:filename")

package you.yearof.app.util

import android.content.Context
import kotlinx.io.files.Path

class AndroidPaths(
    val context: Context,
) : Paths {
    override fun forDatabase(name: String): Path = Path(context.getDatabasePath(name).absolutePath)

    override fun inDocuments(name: String): Path = Path(context.filesDir.resolve(name).absolutePath)
}

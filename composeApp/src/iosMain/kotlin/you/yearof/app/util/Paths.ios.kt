@file:Suppress("ktlint:standard:filename")

package you.yearof.app.util

import kotlinx.io.files.Path
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosPaths : Paths {
    override fun forDatabase(name: String): Path = inDocuments(name)

    override fun inDocuments(name: String): Path {
        val directory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
        return Path(checkNotNull(directory?.path), name)
    }
}

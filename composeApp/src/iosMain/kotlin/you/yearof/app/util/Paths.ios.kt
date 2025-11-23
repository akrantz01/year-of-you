package you.yearof.app.util

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosPaths : Paths {
    override fun forDatabase(name: String): String = inDocuments(name)

    override fun inDocuments(name: String): String {
        val directory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
        return "${checkNotNull(directory?.path)}/$name"
    }
}

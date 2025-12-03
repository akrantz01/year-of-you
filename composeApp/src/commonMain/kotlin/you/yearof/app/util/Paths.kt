package you.yearof.app.util

import kotlinx.io.files.Path
import okio.Path.Companion.toPath

interface Paths {
    fun forDatabase(name: String): Path

    fun inDocuments(name: String): Path
}

fun Path.asOkioPath(): okio.Path = toString().toPath()

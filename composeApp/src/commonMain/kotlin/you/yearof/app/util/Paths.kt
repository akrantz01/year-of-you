package you.yearof.app.util

import kotlinx.io.files.Path

interface Paths {
    fun forDatabase(name: String): Path

    fun inDocuments(name: String): Path
}

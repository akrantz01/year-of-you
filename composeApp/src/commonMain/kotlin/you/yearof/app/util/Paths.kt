package you.yearof.app.util

interface Paths {
    fun forDatabase(name: String): String

    fun inDocuments(name: String): String
}

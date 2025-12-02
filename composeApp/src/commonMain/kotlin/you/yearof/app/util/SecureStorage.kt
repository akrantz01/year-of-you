package you.yearof.app.util

interface SecureStorage {
    suspend fun put(
        key: String,
        value: String?,
    )

    suspend fun get(key: String): String?

    suspend fun clear(key: String)
}

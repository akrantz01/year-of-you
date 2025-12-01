package you.yearof.app.repositories

import you.yearof.app.database.entities.Account

interface AccountRepository {
    suspend fun create(
        displayName: String,
        username: String,
        password: String,
    ): Account

    suspend fun findByUsername(username: String): Account?
}

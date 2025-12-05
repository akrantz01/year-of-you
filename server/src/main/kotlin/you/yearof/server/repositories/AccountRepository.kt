package you.yearof.server.repositories

import you.yearof.server.database.entities.Account

interface AccountRepository {
    suspend fun create(
        displayName: String,
        username: String,
        password: String,
    ): Account

    suspend fun get(id: UInt): Account?

    suspend fun findByUsername(username: String): Account?

    suspend fun update(account: Account)
}

package you.yearof.app.database.repositories

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import you.yearof.app.database.entities.Account
import you.yearof.app.database.tables.Accounts
import you.yearof.app.repositories.AccountRepository

fun provide(db: Database): AccountRepository = SqlAccountRepository(db)

class SqlAccountRepository(
    private val db: Database,
) : AccountRepository {
    override suspend fun create(
        displayName: String,
        username: String,
        password: String,
    ): Account =
        suspendTransaction(db) {
            Account.new {
                this.displayName = displayName
                this.username = username
                this.password = password
            }
        }

    override suspend fun findByUsername(username: String): Account? =
        suspendTransaction(db) {
            Account.find { Accounts.username eq username }.firstOrNull()
        }
}

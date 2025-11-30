package you.yearof.app.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UIntEntity
import org.jetbrains.exposed.v1.dao.UIntEntityClass
import you.yearof.app.database.tables.Accounts

class Account(
    id: EntityID<UInt>,
) : UIntEntity(id) {
    companion object : UIntEntityClass<Account>(Accounts)

    val displayName by Accounts.displayName
    val username by Accounts.username
    val password by Accounts.password
    val lastUpdated by Accounts.lastUpdated
    val createdAt by Accounts.createdAt
}

package you.yearof.server.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UIntEntity
import org.jetbrains.exposed.v1.dao.UIntEntityClass
import you.yearof.server.database.tables.Accounts
import you.yearof.server.database.tables.Captures

class Account(
    id: EntityID<UInt>,
) : UIntEntity(id) {
    companion object : UIntEntityClass<Account>(Accounts)

    var displayName by Accounts.displayName
    var username by Accounts.username
    var password by Accounts.password
    val lastUpdated by Accounts.lastUpdated
    val createdAt by Accounts.createdAt

    val captures by Capture referrersOn Captures.account
}

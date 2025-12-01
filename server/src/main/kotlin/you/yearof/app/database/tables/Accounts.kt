package you.yearof.app.database.tables

import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

const val DisplayNameMaxLength = 64
const val UsernameMaxLength = 64

object Accounts : UIntIdTable(name = "accounts") {
    val displayName = varchar("display_name", DisplayNameMaxLength)
    val username = varchar("username", UsernameMaxLength).uniqueIndex()
    val password = varchar("password", 512)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val lastUpdated = timestamp("last_updated").defaultExpression(CurrentTimestamp)
}

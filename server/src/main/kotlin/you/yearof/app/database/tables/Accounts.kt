package you.yearof.app.database.tables

import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object Accounts : UIntIdTable(name = "accounts") {
    val displayName = varchar("display_name", 64)
    val username = varchar("username", 64)
    val password = varchar("password", 512)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val lastUpdated = timestamp("last_updated").defaultExpression(CurrentTimestamp)
}

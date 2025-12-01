package you.yearof.app.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

object Captures : UIntIdTable(name = "captures") {
    val account = reference("account_id", Accounts, onDelete = ReferenceOption.CASCADE)
    val front = varchar("front", 32)
    val back = varchar("back", 32)
    val swapped = bool("swapped")
    val created_at = timestamp("created_at")
}

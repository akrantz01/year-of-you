package you.yearof.app.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UIntEntity
import org.jetbrains.exposed.v1.dao.UIntEntityClass
import you.yearof.app.database.tables.Captures

class Capture(
    id: EntityID<UInt>,
) : UIntEntity(id) {
    companion object : UIntEntityClass<Capture>(Captures)

    var accountId by Captures.account
    var account by Account referencedOn Captures.account
    var front by Captures.front
    var back by Captures.back
    var swapped by Captures.swapped
    var takenAt by Captures.takenAt
    val uploadedAt by Captures.uploadedAt
}

package you.yearof.app.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UIntEntity
import org.jetbrains.exposed.v1.dao.UIntEntityClass
import you.yearof.app.database.tables.Captures

class Capture(
    id: EntityID<UInt>,
) : UIntEntity(id) {
    companion object : UIntEntityClass<Capture>(Captures)

    val account by Account referencedOn Captures.account
    val front by Captures.front
    val back by Captures.back
    val swapped by Captures.swapped
    val takenAt by Captures.takenAt
    val uploadedAt by Captures.uploadedAt
}

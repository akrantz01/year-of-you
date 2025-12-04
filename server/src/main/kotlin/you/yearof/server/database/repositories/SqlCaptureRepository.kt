package you.yearof.server.database.repositories

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import you.yearof.server.database.entities.Account
import you.yearof.server.database.entities.Capture
import you.yearof.server.database.tables.Captures
import you.yearof.server.repositories.CaptureCursor
import you.yearof.server.repositories.CaptureRepository
import kotlin.time.Instant

class SqlCaptureRepository(
    private val db: Database,
) : CaptureRepository {
    override suspend fun create(
        account: Account,
        front: String,
        back: String,
        swapped: Boolean,
        taken: Instant,
    ): Capture =
        suspendTransaction(db) {
            Capture.new {
                this.account = account
                this.front = front
                this.back = back
                this.swapped = swapped
                this.takenAt = taken
            }
        }

    override suspend fun list(limit: Int, after: CaptureCursor?): List<Capture> =
        suspendTransaction(db) {
            val base =
                if (after == null) {
                    Capture.all()
                } else {
                    Capture.find {
                        (Captures.uploadedAt less after.uploadedAt) or
                        ((Captures.uploadedAt eq after.uploadedAt) and (Captures.id less after.id))
                    }
                }

            base
                .orderBy(
                    Captures.uploadedAt to SortOrder.DESC,
                    Captures.id to SortOrder.DESC,
                )
                .limit(limit)
                .toList()
        }

    override suspend fun get(id: UInt): Capture? = suspendTransaction(db) { Capture.findById(id) }
}

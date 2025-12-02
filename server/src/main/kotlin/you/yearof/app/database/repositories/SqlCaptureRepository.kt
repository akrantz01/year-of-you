package you.yearof.app.database.repositories

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import you.yearof.app.database.entities.Account
import you.yearof.app.database.entities.Capture
import you.yearof.app.repositories.CaptureRepository
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
}

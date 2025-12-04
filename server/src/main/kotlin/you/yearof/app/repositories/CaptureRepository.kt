package you.yearof.app.repositories

import you.yearof.app.database.entities.Account
import you.yearof.app.database.entities.Capture
import kotlin.time.Instant

data class CaptureCursor(
    val uploadedAt: Instant,
    val id: UInt,
)

interface CaptureRepository {
    suspend fun create(
        account: Account,
        front: String,
        back: String,
        swapped: Boolean,
        taken: Instant,
    ): Capture

    suspend fun list(limit: Int, after: CaptureCursor? = null): List<Capture>
}

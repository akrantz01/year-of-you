package you.yearof.server.repositories

import you.yearof.server.database.entities.Account
import you.yearof.server.database.entities.Capture
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

    suspend fun list(
        limit: Int,
        after: CaptureCursor? = null,
    ): List<Capture>

    suspend fun get(id: UInt): Capture?
}

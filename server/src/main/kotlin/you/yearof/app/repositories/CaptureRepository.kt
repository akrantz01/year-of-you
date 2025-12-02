package you.yearof.app.repositories

import you.yearof.app.database.entities.Account
import you.yearof.app.database.entities.Capture
import kotlin.time.Instant

interface CaptureRepository {
    suspend fun create(
        account: Account,
        front: String,
        back: String,
        swapped: Boolean,
        taken: Instant,
    ): Capture
}

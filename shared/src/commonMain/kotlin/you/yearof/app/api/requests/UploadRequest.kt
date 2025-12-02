package you.yearof.app.api.requests

import kotlin.time.Instant

data class UploadRequest(
    val frontPath: String,
    val backPath: String,
    val swapped: Boolean,
    val taken: Instant,
)

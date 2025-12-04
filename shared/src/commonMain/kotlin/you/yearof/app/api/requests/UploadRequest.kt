package you.yearof.app.api.requests

import kotlinx.io.files.Path
import kotlin.time.Instant

data class UploadRequest(
    val frontPath: Path,
    val backPath: Path,
    val swapped: Boolean,
    val taken: Instant,
)

package you.yearof.app.api

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.io.files.Path
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import you.yearof.app.database.RemoteCapture
import you.yearof.app.database.RemoteCaptureDao
import kotlin.time.Instant

@Single
class CaptureService(
    private val api: ApiService,
    @Provided private val remoteCaptures: RemoteCaptureDao,
) {
    fun all(pageSize: Int = 20): Pager<Int, RemoteCapture> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            remoteMediator = CapturesRemoteMediator(api, remoteCaptures, pageSize),
            pagingSourceFactory = { remoteCaptures.paging() },
        )

    suspend fun upload(
        front: Path,
        back: Path,
        swapped: Boolean,
        at: Instant,
        onProgress: ((Float?) -> Unit)? = null,
    ) {
        val onUpload =
            onProgress?.let { cb ->
                { bytesSentTotal: Long, contentLength: Long? ->
                    if (contentLength != null) cb(bytesSentTotal.toFloat() / contentLength.toFloat())
                }
            }

        api.uploadCapture(front, back, swapped, at, onUpload)
    }
}

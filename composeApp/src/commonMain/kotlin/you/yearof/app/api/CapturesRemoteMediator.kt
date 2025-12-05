package you.yearof.app.api

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import you.yearof.shared.api.responses.CaptureListItem
import you.yearof.app.database.RemoteCapture
import you.yearof.app.database.RemoteCaptureDao

private const val DefaultPageSize = 20
private const val CursorId = "remote-capture-feed"

class CapturesRemoteMediator(
    private val api: ApiService,
    private val remoteCaptures: RemoteCaptureDao,
    private val pageSize: Int = DefaultPageSize,
) : RemoteMediator<Int, RemoteCapture>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RemoteCapture>,
    ): MediatorResult =
        try {
            val cursor =
                when (loadType) {
                    LoadType.REFRESH -> null
                    LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                    LoadType.APPEND -> remoteCaptures.getCursor(CursorId)?.nextCursor
                }

            val response = api.allCaptures(limit = state.config.pageSize.coerceAtMost(pageSize), cursor = cursor)
            val entities = response.items.map(::mapCapture)

            remoteCaptures.update(
                cursorId = CursorId,
                load = loadType,
                entities = entities,
                nextCursor = response.nextCursor,
            )

            MediatorResult.Success(endOfPaginationReached = response.nextCursor == null)
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
}

private fun mapCapture(item: CaptureListItem): RemoteCapture =
    RemoteCapture(
        id = item.id.toLong(),
        accountId = item.account.id.toLong(),
        accountUsername = item.account.username,
        frontUrl = item.front,
        backUrl = item.back,
        swapped = item.swapped,
        takenAt = item.takenAt,
        uploadedAt = item.uploadedAt,
    )

package you.yearof.app.database

import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlin.time.Instant

@Entity(tableName = "remote_captures")
data class RemoteCapture(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "front_url") val frontUrl: String,
    @ColumnInfo(name = "back_url") val backUrl: String,
    val swapped: Boolean,
    @ColumnInfo(name = "taken_at") val takenAt: Instant,
    @ColumnInfo(name = "uploaded_at") val uploadedAt: Instant,
)

@Dao
abstract class RemoteCaptureDao : RemoteCursorAccessDao() {
    @Query("SELECT * FROM remote_captures ORDER BY uploaded_at DESC, id DESC")
    abstract fun paging(): PagingSource<Int, RemoteCapture>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(items: List<RemoteCapture>)

    @Query("DELETE FROM remote_captures")
    abstract suspend fun clearAll()

    @Transaction
    open suspend fun update(
        cursorId: String,
        load: LoadType,
        entities: List<RemoteCapture>,
        nextCursor: String? = null,
    ) {
        if (load == LoadType.REFRESH) {
            clearAll()
            clearCursor(cursorId)
        }

        upsertAll(entities)
        setCursor(RemoteCursor(cursorId, nextCursor))
    }
}

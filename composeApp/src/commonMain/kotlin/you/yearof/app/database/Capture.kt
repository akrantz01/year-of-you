package you.yearof.app.database

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.io.files.Path
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(tableName = "captures")
data class Capture(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "front_path") val frontPath: Path,
    @ColumnInfo(name = "back_path") val backPath: Path,
    @ColumnInfo(name = "at") val at: Instant,
    @ColumnInfo(defaultValue = "false") val swapped: Boolean = false,
    @ColumnInfo(defaultValue = "") val caption: String = "",
    @ColumnInfo(defaultValue = "false") val shared: Boolean = false,
    @ColumnInfo(name = "uploaded_at", defaultValue = "null") val uploadedAt: Instant? = null,
)

@Dao
interface CaptureDao {
    @Insert
    suspend fun insert(capture: Capture)

    @Query("SELECT * FROM captures ORDER BY at DESC")
    fun all(): PagingSource<Int, Capture>
}

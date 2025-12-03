package you.yearof.app.database

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlin.time.Instant

@Entity(tableName = "captures")
data class Capture(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "front_path") val frontPath: String,
    @ColumnInfo(name = "back_path") val backPath: String,
    @ColumnInfo(name = "at") val at: Instant,
    @ColumnInfo(defaultValue = "false") val swapped: Boolean = false,
    @ColumnInfo(defaultValue = "") val caption: String = "",
)

@Dao
interface CaptureDao {
    @Insert
    suspend fun insert(capture: Capture)

    @Query("SELECT * FROM captures ORDER BY at DESC")
    fun all(): PagingSource<Int, Capture>
}

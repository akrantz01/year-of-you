package you.yearof.app.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "captures")
data class Capture(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "front_path") val frontPath: String,
    @ColumnInfo(name = "back_path") val backPath: String,
    @ColumnInfo(name = "at") val atMillis: Long,
) {
    val at: Instant
        get() = Instant.fromEpochMilliseconds(atMillis)
}

@Dao
interface CaptureDao {
    @Insert
    suspend fun insert(capture: Capture)
}

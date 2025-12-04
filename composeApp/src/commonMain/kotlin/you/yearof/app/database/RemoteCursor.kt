package you.yearof.app.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "remote_cursors")
data class RemoteCursor(
    @PrimaryKey val id: String,
    val nextCursor: String?,
)

@Dao
abstract class RemoteCursorAccessDao {
    @Query("SELECT * FROM remote_cursors WHERE id = :id LIMIT 1")
    abstract suspend fun getCursor(id: String): RemoteCursor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun setCursor(cursor: RemoteCursor)

    @Query("DELETE FROM remote_cursors WHERE id = :id")
    protected abstract suspend fun clearCursor(id: String)
}

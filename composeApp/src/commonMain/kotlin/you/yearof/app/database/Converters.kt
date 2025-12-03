package you.yearof.app.database

import androidx.room.TypeConverter
import kotlinx.io.files.Path
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun instantFromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun instantToTimestamp(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun pathToString(path: Path?): String? = path?.toString()

    @TypeConverter
    fun pathFromString(value: String?): Path? = value?.let { Path(it) }
}

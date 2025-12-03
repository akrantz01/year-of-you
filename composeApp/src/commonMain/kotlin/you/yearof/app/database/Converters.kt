package you.yearof.app.database

import androidx.room.TypeConverter
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun toTimestamp(value: Instant?): Long? = value?.toEpochMilliseconds()
}

package you.yearof.app.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Composable
actual fun rememberDatabase(): AppDatabase {
    val path = "${documentDirectory()}/$DatabaseName"
    return remember {
        Room
            .databaseBuilder<AppDatabase>(name = path)
            .apply(RoomDatabase.Builder<AppDatabase>::commonConfiguration)
            .build()
    }
}

private fun documentDirectory(): String {
    val directory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    return requireNotNull(directory?.path)
}

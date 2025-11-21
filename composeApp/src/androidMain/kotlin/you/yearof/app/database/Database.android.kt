package you.yearof.app.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.room.RoomDatabase

@Composable
actual fun rememberDatabase(): AppDatabase {
    val context = LocalContext.current
    val path = context.getDatabasePath(DatabaseName).absolutePath

    return remember {
        Room
            .databaseBuilder<AppDatabase>(
                context = context,
                name = path,
            ).apply(RoomDatabase.Builder<AppDatabase>::commonConfiguration)
            .build()
    }
}

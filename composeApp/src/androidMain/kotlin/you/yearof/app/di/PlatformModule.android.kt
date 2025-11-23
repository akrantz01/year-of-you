package you.yearof.app.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import you.yearof.app.BuildConfig
import you.yearof.app.database.AppDatabase
import you.yearof.app.database.buildDatabase
import you.yearof.app.util.AndroidPaths
import you.yearof.app.util.Paths

actual val platformModule: Module =
    module {
        singleOf(::AndroidPaths) {
            binds(listOf(Paths::class))
        }

        singleOf({ context: Context, paths: Paths ->
            val driver = if (BuildConfig.USE_NATIVE_SQLITE_DRIVER) AndroidSQLiteDriver() else BundledSQLiteDriver()
            buildDatabase(driver = driver) { name ->
                val path = paths.forDatabase(name)
                Room.databaseBuilder(context = context, name = path)
            }
        }) {
            binds(listOf(AppDatabase::class))
        }
    }

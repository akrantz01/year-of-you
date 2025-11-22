package you.yearof.app.di

import androidx.room.Room
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import you.yearof.app.database.AppDatabase
import you.yearof.app.database.buildDatabase
import you.yearof.app.util.IosPaths
import you.yearof.app.util.Paths

actual val platformModule =
    module {
        singleOf(::IosPaths) {
            binds(listOf(Paths::class))
        }

        singleOf({ paths: Paths ->
            buildDatabase { name ->
                val path = paths.forDatabase(name)
                Room.databaseBuilder(name = path)
            }
        }) {
            binds(listOf(AppDatabase::class))
        }
    }

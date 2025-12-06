package you.yearof.app.database

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided

@Module
class DatabaseModule {
    @Factory
    fun captures(
        @Provided database: AppDatabase,
    ): CaptureDao = database.captures()

    @Factory
    fun remoteCaptures(
        @Provided database: AppDatabase,
    ): RemoteCaptureDao = database.remoteCaptures()
}

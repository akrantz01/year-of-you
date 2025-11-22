package you.yearof.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import you.yearof.app.di.startKoin

class YearOfYou : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@YearOfYou)
        }
    }
}

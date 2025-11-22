package you.yearof.app.di

import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.ksp.generated.startKoin
import you.yearof.app.database.DatabaseModule

@KoinApplication(modules = [CommonModule::class, DatabaseModule::class])
object Koin

fun startKoin(config: KoinAppDeclaration? = null) {
    Koin.startKoin {
        printLogger()
        includes(config)
        modules(platformModule)
    }
}

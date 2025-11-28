package you.yearof.app.database

import io.ktor.util.logging.KtorSimpleLogger
import org.flywaydb.core.Flyway
import javax.sql.DataSource

private val LOGGER = KtorSimpleLogger("you.yearof.app.database.Migrations")

internal fun migrate(dataSource: DataSource) {
    LOGGER.info("running migrations...")
    val flyway =
        Flyway
            .configure()
            .envVars()
            .dataSource(dataSource)
            .locations("classpath:migrations")
            .load()

    val info = flyway.info()
    val current = info.current()
    if (current != null) {
        LOGGER.info("current migration: ${current.version} ${current.description}")
    }

    val pending = info.pending()
    if (pending.isNotEmpty()) {
        LOGGER.info("pending migrations:")
        pending.forEach {
            LOGGER.info("\t${it.version}: ${it.description}")
        }
    }

    flyway.migrate()
    flyway.validate()
}

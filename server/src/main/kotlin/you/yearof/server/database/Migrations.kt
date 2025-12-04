package you.yearof.server.database

import io.ktor.util.logging.KtorSimpleLogger
import org.flywaydb.core.Flyway
import you.yearof.server.database.config.DatabaseDriver
import javax.sql.DataSource

private val LOGGER = KtorSimpleLogger("you.yearof.server.database.Migrations")

internal fun migrate(
    driver: DatabaseDriver,
    dataSource: DataSource,
) {
    LOGGER.info("running migrations...")
    val flyway =
        Flyway
            .configure()
            .envVars()
            .dataSource(dataSource)
            .locations("classpath:migrations")
            .placeholders(driver.placeholders())
            .baselineOnMigrate(true)
            .validateOnMigrate(true)
            .cleanDisabled(true)
            .load()

    val info = flyway.info()
    val current = info.current()
    if (current != null) {
        LOGGER.info("current migration: ${current.version} - ${current.description}")
    }

    val pending = info.pending()
    if (pending.isNotEmpty()) {
        LOGGER.info("pending migrations:")
        pending.forEach {
            LOGGER.info("\t${it.version} - ${it.description}")
        }
    }

    val result = flyway.migrate()
    LOGGER.info("migrations complete!")
    LOGGER.info("initial version=${result.initialSchemaVersion} target version=${result.targetSchemaVersion}")
}

package you.yearof.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs
import io.ktor.server.plugins.di.annotations.Property
import org.jetbrains.exposed.v1.jdbc.Database
import you.yearof.server.database.config.DatabaseConfig
import javax.sql.DataSource

fun provideDatabase(
    @Property("database") config: DatabaseConfig,
): Database {
    val dataSource = createConnectionPool(config)

    if (config.migrate) migrate(config.driver, dataSource)

    return Database.connect(dataSource)
}

fun ApplicationConfig.createConnectionPool(): DataSource = createConnectionPool(config = property("database").getAs())

private fun createConnectionPool(config: DatabaseConfig): DataSource {
    val pool = HikariConfig()
    pool.jdbcUrl = config.url
    config.username?.let { pool.username = it }
    config.password?.let { pool.password = it }
    config.pool.apply(pool)

    return HikariDataSource(pool)
}

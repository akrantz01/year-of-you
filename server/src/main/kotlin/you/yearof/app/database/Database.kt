package you.yearof.app.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.config.property
import org.jetbrains.exposed.v1.jdbc.Database
import you.yearof.app.database.config.DatabaseConfig

fun Application.initializeDatabase() {
    val config = property<DatabaseConfig>("database")

    val pool = HikariConfig()
    pool.jdbcUrl = config.url
    config.username?.let { pool.username = it }
    config.password?.let { pool.password = it }
    config.pool.apply(pool)

    Database.connect(datasource = HikariDataSource(pool))
}

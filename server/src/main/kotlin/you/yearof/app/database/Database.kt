package you.yearof.app.database

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfigValue
import io.ktor.server.config.property
import io.ktor.server.config.propertyOrNull
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.Option
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import you.yearof.app.util.serializers.CaseInsensitiveSerializer

@Serializable(with = DatabaseDriver.AsLowercase::class)
enum class DatabaseDriver {
    H2,
    MariaDB,
    MySQL,
    PostgreSQL,
    ;

    internal object AsLowercase : CaseInsensitiveSerializer<DatabaseDriver>(DatabaseDriver::class)
}

fun Application.initializeDatabase() {
    val databaseConfig = environment.config.propertyOrNull("database")
    val options =
        when (databaseConfig?.type) {
            ApplicationConfigValue.Type.SINGLE -> ConnectionFactoryOptions.parse(databaseConfig.getString())
            ApplicationConfigValue.Type.OBJECT -> databaseFromObject()
            else -> throw IllegalArgumentException("database must be a string or object")
        }

    R2dbcDatabase.connect(
        databaseConfig =
            R2dbcDatabaseConfig {
                defaultMaxAttempts = 1
                defaultR2dbcIsolationLevel = IsolationLevel.READ_COMMITTED
                connectionFactoryOptions = options
            },
    )
}

private fun Application.databaseFromObject(): ConnectionFactoryOptions {
    val driver = property<DatabaseDriver>("database.driver")

    val builder =
        ConnectionFactoryOptions
            .builder()
            .option(ConnectionFactoryOptions.DRIVER, driver.name.lowercase())

    propertyOrNull<String>("database.host")?.let { host ->
        builder.option(ConnectionFactoryOptions.HOST, host)
    }
    propertyOrNull<String>("database.protocol")?.let { protocol ->
        builder.option(ConnectionFactoryOptions.PROTOCOL, protocol)
    }
    propertyOrNull<Int>("database.port")?.let { port ->
        builder.option(ConnectionFactoryOptions.PORT, port)
    }
    propertyOrNull<String>("database.user")?.let { user ->
        builder.option(ConnectionFactoryOptions.USER, user)
    }
    propertyOrNull<String>("database.password")?.let { password ->
        builder.option(ConnectionFactoryOptions.PASSWORD, password)
    }
    propertyOrNull<String>("database.database")?.let { database ->
        builder.option(ConnectionFactoryOptions.DATABASE, database)
    }

    val options = propertyOrNull<Map<String, String>>("database.options") ?: emptyMap()
    for ((key, value) in options) {
        builder.option(Option.valueOf(key), value)
    }

    return builder.build()
}

package you.yearof.server.database.config

import java.net.URI

enum class DatabaseDriver {
    H2 {
        override fun placeholders(): Map<String, String> = mapOf("autoincrement" to "AUTO_INCREMENT")
    },
    MariaDB {
        override fun placeholders(): Map<String, String> = mapOf("autoincrement" to "AUTO_INCREMENT")
    },
    MySQL {
        override fun placeholders(): Map<String, String> = mapOf("autoincrement" to "AUTO_INCREMENT")
    },
    PostgreSQL {
        override fun placeholders(): Map<String, String> = mapOf("autoincrement" to "GENERATED ALWAYS AS IDENTITY")
    },
    ;

    abstract fun placeholders(): Map<String, String>

    companion object {
        fun fromUrl(url: String): DatabaseDriver {
            val uri = URI(url)
            check(uri.scheme == "jdbc") { "database URL must be a JDBC connection URL" }

            val driver =
                uri.schemeSpecificPart
                    .split(':', limit = 2)
                    .map { it.lowercase() }
                    .firstOrNull()
            checkNotNull(driver) { "missing driver in database URL" }

            return when (driver.lowercase()) {
                "h2" -> H2
                "mariadb" -> MariaDB
                "mysql" -> MySQL
                "postgresql" -> PostgreSQL
                else -> throw IllegalArgumentException("unknown database driver: $driver")
            }
        }
    }
}

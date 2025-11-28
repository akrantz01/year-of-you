package you.yearof.app.database.config

import kotlinx.serialization.Serializable
import you.yearof.app.util.serializers.NullableStringSerializer
import java.net.URI

@Serializable
data class DatabaseConfig(
    val url: String,
    @Serializable(with = NullableStringSerializer::class)
    val username: String? = null,
    @Serializable(with = NullableStringSerializer::class)
    val password: String? = null,
    val migrate: Boolean = true,
    val pool: DatabasePoolConfig = DatabasePoolConfig(),
) {
    init {
        val uri = URI(url)
        check(uri.scheme == "jdbc") { "database URL must be a JDBC connection URL" }

        val driver =
            uri.schemeSpecificPart
                .split(':', limit = 2)
                .map { it.lowercase() }
                .firstOrNull()
        checkNotNull(driver) { "missing driver in database URL" }
        check(drivers.contains(driver)) { "unknown database driver '$driver'" }
    }

    companion object {
        private val drivers = setOf("h2", "mariadb", "mysql", "postgresql")
    }
}

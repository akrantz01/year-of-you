package you.yearof.server.database.config

import kotlinx.serialization.Serializable
import you.yearof.server.util.serializers.NullableStringSerializer

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
    val driver = DatabaseDriver.fromUrl(url)
}

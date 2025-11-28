package you.yearof.app.database.config

import com.zaxxer.hikari.HikariConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
data class DatabasePoolConfig(
    @SerialName("max-size")
    val maxSize: Int = 10,
    @SerialName("min-idle")
    val minIdle: Int = 0,
    @SerialName("idle-timeout")
    val idleTimeout: Duration = 10.minutes,
    @SerialName("max-lifetime")
    val maxLifetime: Duration = 30.minutes,
    @SerialName("connection-timeout")
    val connectionTimeout: Duration = 30.seconds,
    @SerialName("validation-timeout")
    val validationTimeout: Duration = 5.seconds,
    @SerialName("validation-query")
    val validationQuery: String = "SELECT 1",
) {
    init {
        check(maxSize > 1) { "maximum pool size cannot be less than 1" }
        check(minIdle >= 0) { "minimum idle connections cannot be less than 0" }
        checkDuration(idleTimeout, "connection idle timeout")
        checkDuration(maxLifetime, "maximum connection lifetime")
        checkDuration(connectionTimeout, "connection timeout")
        checkDuration(validationTimeout, "validation timeout")
        check(validationQuery.isNotBlank()) { "connection validation query cannot be blank" }
    }

    fun apply(pool: HikariConfig) {
        pool.maximumPoolSize = maxSize
        pool.minimumIdle = minIdle
        pool.idleTimeout = idleTimeout.inWholeMilliseconds
        pool.maxLifetime = maxLifetime.inWholeMilliseconds
        pool.connectionTimeout = connectionTimeout.inWholeMilliseconds
        pool.validationTimeout = validationTimeout.inWholeMilliseconds
        pool.connectionTestQuery = validationQuery
    }

    companion object {
        private fun checkDuration(
            duration: Duration,
            name: String,
        ) {
            check(duration.isPositive()) { "$name must be positive" }
            check(duration.isFinite()) { "$name must be finite" }
        }
    }
}

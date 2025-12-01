package you.yearof.app.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.di.annotations.Property
import kotlinx.serialization.Serializable
import you.yearof.app.database.entities.Account
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid

private const val Audience = "app"

@Serializable
data class TokenConfig(
    val issuer: String,
    val secret: String,
)

fun provide(
    @Property("jwt") config: TokenConfig,
): TokenService {
    check(config.issuer.isNotBlank()) { "issuer can not be blank" }
    check(config.secret.isNotBlank()) { "signing secret can not be blank" }
    check(config.secret.length >= 32) { "signing secret must be at least 32 bytes" }

    return TokenService(config)
}

enum class TokenUsage {
    Access,
    Refresh,
}

data class IssuedTokens(
    val id: String,
    val accessToken: String,
    val refreshToken: String,
)

data class RefreshedTokens(
    val accessToken: String,
    val refreshToken: String?,
)

class TokenService(
    config: TokenConfig,
) {
    private val issuer = config.issuer
    private val algorithm = Algorithm.HMAC256(config.secret)

    val verifier: JWTVerifier =
        JWT
            .require(algorithm)
            .withAudience(Audience)
            .withIssuer(issuer)
            .build()

    fun issue(account: Account): IssuedTokens {
        val id = Uuid.random().toString()

        return IssuedTokens(
            id,
            accessToken =
                newToken(
                    id = id,
                    subject = account.id.value,
                    lifetime = 7.days,
                    type = TokenUsage.Access,
                ),
            refreshToken =
                newToken(
                    id = id,
                    subject = account.id.value,
                    lifetime = 30.days,
                    type = TokenUsage.Refresh,
                ),
        )
    }

    fun refresh(
        account: Account,
        current: JWTPrincipal,
    ): RefreshedTokens {
        val id = current.jwtId!!

        return RefreshedTokens(
            accessToken = newToken(id = id, subject = account.id.value, lifetime = 7.days, type = TokenUsage.Access),
            refreshToken = null, // TODO: re-generate refresh token when nearing expiration (within 1 week)
        )
    }

    fun validate(
        requiredUsage: TokenUsage,
        credential: JWTCredential,
    ): JWTPrincipal? {
        val scope = credential.payload.getClaim("scope").asString()
        val usage = TokenUsage.valueOf(scope)

        return if (usage == requiredUsage) {
            JWTPrincipal(credential.payload)
        } else {
            null
        }
    }

    private fun newToken(
        id: String,
        subject: UInt,
        lifetime: Duration,
        type: TokenUsage,
    ): String {
        val now = Clock.System.now()
        return JWT
            .create()
            .withJWTId(id)
            .withSubject(subject.toString())
            .withAudience(Audience)
            .withIssuer(issuer)
            .withNotBefore(now.toJavaInstant())
            .withIssuedAt(now.toJavaInstant())
            .withExpiresAt(now.plus(lifetime).toJavaInstant())
            .withClaim("scope", type.name)
            .sign(algorithm)
    }
}

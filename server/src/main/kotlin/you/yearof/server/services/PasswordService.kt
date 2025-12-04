package you.yearof.server.services

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder

enum class VerificationResult {
    Valid,
    NeedsUpgrade,
    Invalid,
    ;

    val ok: Boolean
        get() =
            when (this) {
                Valid, NeedsUpgrade -> true
                else -> false
            }
}

object PasswordService {
    private val encoder =
        DelegatingPasswordEncoder(
            "argon2id",
            mapOf(
                "argon2id" to Argon2PasswordEncoder(16, 32, 1, 12 * 1024, 3),
            ),
        )

    fun hash(password: String): String = checkNotNull(encoder.encode(password))

    fun verify(
        password: String,
        hash: String,
    ): VerificationResult =
        if (!encoder.matches(password, hash)) {
            VerificationResult.Invalid
        } else if (encoder.upgradeEncoding(hash)) {
            VerificationResult.NeedsUpgrade
        } else {
            VerificationResult.Valid
        }
}

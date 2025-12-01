package you.yearof.app.passwords

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

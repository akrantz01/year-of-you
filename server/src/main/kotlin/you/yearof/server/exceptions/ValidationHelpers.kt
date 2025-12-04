@file:OptIn(ExperimentalContracts::class)

package you.yearof.server.exceptions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline fun validate(value: Boolean, lazyMessage: () -> String = { "bad request" }) {
    contract {
        returns() implies value
    }
    if (!value) throw BadRequestException(lazyMessage())
}

inline fun <T : Any> validateNotNull(value: T?, lazyMessage: () -> String = { "bad request" }): T {
    contract {
        returns() implies (value != null)
    }
    if (value == null) throw BadRequestException(lazyMessage())
    return value
}

// TODO: support keyed validator for better error messages

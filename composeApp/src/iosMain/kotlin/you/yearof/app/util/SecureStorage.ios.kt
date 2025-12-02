package you.yearof.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle

class IosSecureStorage : SecureStorage {
    private val service = requireNotNull(NSBundle.mainBundle.bundleIdentifier) { "missing bundle identifier" }

    override suspend fun put(
        key: String,
        value: String?,
    ) = withContext(Dispatchers.Default) {
        TODO("Not yet implemented")
    }

    override suspend fun get(key: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun clear(key: String) {
        TODO("Not yet implemented")
    }
}

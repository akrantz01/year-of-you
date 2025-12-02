package you.yearof.app.util

import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFAutorelease
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecAttrSynchronizable
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.OSStatus
import platform.darwin.noErr
import platform.posix.memcpy

class IosSecureStorage : SecureStorage {
    private val service = requireNotNull(NSBundle.mainBundle.bundleIdentifier) { "missing bundle identifier" }

    override suspend fun put(
        key: String,
        value: String?,
    ) = withContext(Dispatchers.Default) {
        val data = value?.toByteArray()?.toNSData()
        if (data != null) {
            if (exists(key)) update(key, data) else add(key, data)
        } else {
            delete(key)
        }
    }

    override suspend fun get(key: String): String? = withContext(Dispatchers.Default) {
        value(key)?.toByteArray()?.decodeToString()
    }

    override suspend fun clear(key: String) = withContext(Dispatchers.Default) {
        delete(key)
    }

    private fun exists(key: String): Boolean = context(key) { (account) ->
        val query = query(
            kSecAttrAccount to account,
            kSecReturnData to kCFBooleanFalse,
        )
        when (val status = SecItemCopyMatching(query, null)) {
            noErr.toInt() -> true
            errSecItemNotFound.toInt() -> false
            else -> throw KeychainException("exists $key", status)
        }
    }

    private fun add(key: String, value: NSData?): Unit = context(key, value) { (account, data) ->
        val query = query(
            kSecAttrAccount to account,
            kSecValueData to data,
        )
        SecItemAdd(query, null).throwIfFailed("add $key")
    }

    private fun update(key: String, value: Any?): Unit = context(key, value) { (account, data) ->
        val query = query(
            kSecAttrAccount to account,
            kSecReturnData to kCFBooleanFalse,
        )
        val toUpdate = query(kSecValueData to data)
        SecItemUpdate(query, toUpdate).throwIfFailed("update $key")
    }

    private fun value(key: String): NSData? = context(key) { (account) ->
        val query = query(
            kSecAttrAccount to account,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )
        memScoped {
            val result = alloc<CFTypeRefVar>()
            result.value = null
            when (val status = SecItemCopyMatching(query, result.ptr)) {
                noErr.toInt() -> CFBridgingRelease(result.value) as? NSData
                errSecItemNotFound.toInt() -> null
                else -> throw KeychainException("get $key", status)
            }
        }
    }

    private fun delete(key: String): Unit = context(key) { (account) ->
        val query = query(kSecAttrAccount to account)
        when (val status = SecItemDelete(query)) {
            noErr.toInt(), errSecItemNotFound.toInt() -> Unit
            else -> throw KeychainException("delete $key", status)
        }
    }

    private class Context(val refs: Map<CFStringRef?, CFTypeRef?>) {
        fun query(vararg pairs: Pair<CFStringRef?, CFTypeRef?>): CFDictionaryRef? {
            val map = mapOf(*pairs).plus(refs.filter { it.value != null })
            return CFDictionaryCreateMutable(null, map.size.convert(), null, null)
                .apply { map.entries.forEach { CFDictionaryAddValue(this, it.key, it.value) } }
                .apply { CFAutorelease(this) }
        }
    }

    private fun <T> context(vararg values: Any?, block: Context.(List<CFTypeRef?>) -> T): T {
        val serviceRef = CFBridgingRetain(service)
        val standard = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceRef,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            kSecAttrSynchronizable to kCFBooleanFalse!!,
        )
        val custom = arrayOf(*values).map { value -> value?.let { CFBridgingRetain(it) } }
        val retained = listOfNotNull(serviceRef) + custom.filterNotNull()

        return try {
            block.invoke(Context(standard), custom)
        } finally {
            retained.forEach { CFBridgingRelease(it) }
        }
    }

    private fun ByteArray.toNSData(): NSData = memScoped {
        NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.convert())
    }

    private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            usePinned {
                memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
            }
        }
    }

    private fun OSStatus.throwIfFailed(action: String) {
        if (this != noErr.toInt()) throw KeychainException(action, this)
    }

    class KeychainException(action: String, status: OSStatus) :
        RuntimeException("$action failed (status=$status)")
}

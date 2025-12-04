package you.yearof.app.util

import android.content.Context
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.TinkProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeystore
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.security.GeneralSecurityException

private const val KeysetFileName = "secure_storage_keyset.json"
private const val MasterKeyAlias = "secure_storage_master_key"

private val Context.secureDataStore by preferencesDataStore("secure-storage")

class AndroidSecureStorage(
    private val context: Context,
) : SecureStorage {
    private val datastore = context.secureDataStore
    private val aead by lazy { initAead() }

    override suspend fun put(
        key: String,
        value: String?,
    ) {
        val key = byteArrayPreferencesKey(key)
        val value = value?.let { aead.encrypt(it.toByteArray(), null) }

        datastore.edit { prefs ->
            if (value == null) {
                prefs.remove(key)
            } else {
                prefs[key] = value
            }
        }
    }

    override suspend fun has(key: String): Boolean =
        datastore.data
            .firstOrNull()
            ?.contains(byteArrayPreferencesKey(key))
            ?: false

    override suspend fun get(key: String): String? =
        datastore.data
            .firstOrNull()
            ?.get(byteArrayPreferencesKey(key))
            ?.let { aead.decrypt(it, null) }
            ?.decodeToString()

    override suspend fun clear(key: String) {
        datastore.edit { prefs ->
            prefs.remove(byteArrayPreferencesKey(key))
        }
    }

    private fun initAead(): Aead {
        AeadConfig.register()

        val masterAead = ensureMasterAead()
        val keysetFile = File(context.noBackupFilesDir, KeysetFileName)

        val handle = runCatching {
            val bytes = keysetFile.readBytes()
            TinkProtoKeysetFormat.parseEncryptedKeyset(bytes, masterAead, ByteArray(0))
        }.getOrElse {
            KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM).also { handle ->
                val serialized = TinkProtoKeysetFormat.serializeEncryptedKeyset(handle, masterAead, ByteArray(0))
                keysetFile.writeBytes(serialized)
            }
        }

        return handle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private fun ensureMasterAead(): Aead {
        return try {
            if (!AndroidKeystore.hasKey(MasterKeyAlias)) {
                AndroidKeystore.generateNewAes256GcmKey(MasterKeyAlias)
            }
            AndroidKeystore.getAead(MasterKeyAlias)
        } catch (e: GeneralSecurityException) {
            AndroidKeystore.deleteKey(MasterKeyAlias)
            AndroidKeystore.generateNewAes256GcmKey(MasterKeyAlias)
            AndroidKeystore.getAead(MasterKeyAlias)
        }
    }
}

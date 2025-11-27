package you.yearof.app.util.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

open class CaseInsensitiveSerializer<T : Enum<T>>(
    kClass: KClass<T>,
) : KSerializer<T> {
    private val name = kClass.simpleName ?: "Enum"
    private val members = kClass.java.enumConstants
    private val memberNames = members.joinToString { it.name.lowercase() }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(name, PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) = encoder.encodeString(value.name.lowercase())

    override fun deserialize(decoder: Decoder): T {
        val s = decoder.decodeString()
        return members.firstOrNull { it.name.equals(s, ignoreCase = true) }
            ?: throw IllegalArgumentException(
                "Unknown $name value: '$s' (allowed: $memberNames)",
            )
    }
}

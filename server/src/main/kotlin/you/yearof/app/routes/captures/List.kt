package you.yearof.app.routes.captures

import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import you.yearof.app.api.Routes
import you.yearof.app.exceptions.BadRequestException
import you.yearof.app.api.responses.CaptureListItem
import you.yearof.app.api.responses.CapturePage
import you.yearof.app.exceptions.validate
import you.yearof.app.repositories.CaptureCursor
import you.yearof.app.repositories.CaptureRepository
import java.util.Base64
import kotlin.time.Instant

internal fun Route.listRoute(captures: CaptureRepository) {
    get<Routes.Captures> { capture ->
        validate(capture.limit > 0) { "limit must be positive" }

        val cursor =
            capture.cursor?.let { encoded ->
                runCatching { decodeCursor(encoded) }.getOrElse { throw BadRequestException("invalid cursor") }
            }
        val all = captures.list(limit = capture.limit, after = cursor).map {
            CaptureListItem(
                id = it.id.value,
                accountId = it.accountId.value,
                // TODO: build URLs for front and back
                front = it.front,
                back = it.back,
                swapped = it.swapped,
                takenAt = it.takenAt,
                uploadedAt = it.uploadedAt,
            )
        }

        val nextCursor =
            if (all.size < capture.limit) {
                null
            } else {
                all.lastOrNull()?.let {
                    encodeCursor(CaptureCursor(uploadedAt = it.uploadedAt, id = it.id))
                }
            }

        call.respond(CapturePage(items = all, nextCursor = nextCursor))
    }
}

private fun decodeCursor(encoded: String): CaptureCursor {
    val decoded = Base64.getUrlDecoder().decode(encoded).decodeToString()
    val parts = decoded.split("|")
    validate(parts.size == 2) { "invalid cursor format" }

    val uploadedAt = Instant.parse(parts[0])
    val id = parts[1].toUInt()
    return CaptureCursor(uploadedAt = uploadedAt, id = id)
}

private fun encodeCursor(cursor: CaptureCursor): String =
    Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString("${cursor.uploadedAt}|${cursor.id}".encodeToByteArray())

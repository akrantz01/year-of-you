package you.yearof.app.routes.captures

import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64
import you.yearof.app.api.Routes
import you.yearof.app.exceptions.BadRequestException
import you.yearof.app.api.responses.CaptureListItem
import you.yearof.app.api.responses.CapturePage
import you.yearof.app.exceptions.validate
import you.yearof.app.repositories.CaptureCursor
import you.yearof.app.repositories.CaptureRepository
import you.yearof.app.util.href
import kotlin.time.Instant

internal fun Route.listRoute(captures: CaptureRepository) {
    get<Routes.Captures.List> { route ->
        validate(route.limit > 0) { "limit must be positive" }

        val cursor =
            route.cursor?.let { encoded ->
                runCatching { decodeCursor(encoded) }.getOrElse { throw BadRequestException("invalid cursor") }
            }
        val all = captures.list(limit = route.limit, after = cursor).map {
            val id = it.id.value
            CaptureListItem(
                id = id,
                accountId = it.accountId.value,
                front = href(Routes.Captures.Id.Front.make(id)),
                back = href(Routes.Captures.Id.Back.make(id)),
                swapped = it.swapped,
                takenAt = it.takenAt,
                uploadedAt = it.uploadedAt,
            )
        }

        val nextCursor =
            if (all.size < route.limit) {
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
    val decoded = encoded.decodeBase64String()
    val parts = decoded.split("|")
    validate(parts.size == 2) { "invalid cursor format" }

    val uploadedAt = Instant.parse(parts[0])
    val id = parts[1].toUInt()
    return CaptureCursor(uploadedAt = uploadedAt, id = id)
}

private fun encodeCursor(cursor: CaptureCursor): String = "${cursor.uploadedAt}|${cursor.id}".encodeBase64()

package you.yearof.app.routes.captures

import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import you.yearof.app.api.Routes
import you.yearof.app.api.responses.CaptureList
import you.yearof.app.repositories.CaptureRepository

internal fun Route.listRoute(captures: CaptureRepository) {
    get<Routes.Captures> { capture ->
        require(capture.limit >= 0) { "limit must be positive" }
        require(capture.offset >= 0) { "offset must be positive" }

        val all = captures.list(capture.limit, capture.offset).map {
            CaptureList(
                accountId = it.accountId.value,
                // TODO: build URLs for front and back
                front = it.front,
                back = it.back,
                swapped = it.swapped,
                takenAt = it.takenAt,
                uploadedAt = it.uploadedAt,
            )
        }
        call.respond(all)
    }
}

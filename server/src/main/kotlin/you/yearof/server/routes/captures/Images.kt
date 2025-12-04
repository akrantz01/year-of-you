package you.yearof.server.routes.captures

import io.ktor.http.ContentType
import io.ktor.server.http.content.LocalFileContent
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import you.yearof.shared.api.Routes
import you.yearof.server.exceptions.NotFoundException
import you.yearof.server.repositories.CaptureRepository
import java.io.File

fun Route.imagesRoutes(captures: CaptureRepository) {
    // TODO: abstract into file service
    val uploads = File("uploads")

    get<Routes.Captures.Id.Front> { route ->
        val capture = captures.get(route.parent.id) ?: throw NotFoundException("not found")
        call.respond(LocalFileContent(uploads, capture.front, ContentType.Image.JPEG))
    }

    get<Routes.Captures.Id.Back> { route ->
        val capture = captures.get(route.parent.id) ?: throw NotFoundException("not found")
        call.respond(LocalFileContent(uploads, capture.back, ContentType.Image.JPEG))
    }
}

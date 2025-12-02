package you.yearof.app.routes.captures

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveMultipart
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import you.yearof.app.api.Routes
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.repositories.CaptureRepository
import java.io.File
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val UploadLimit: Long = 1024 * 1024 * 16

data class UploadRequest(
    val frontPath: String,
    val backPath: String,
    val swapped: Boolean,
    val taken: Instant,
)

internal fun Route.uploadRoute(
    accounts: AccountRepository,
    captures: CaptureRepository,
) {
    post<Routes.Captures> {
        val builder = UploadRequestBuilder()
        call.receiveMultipart(formFieldLimit = UploadLimit).forEachPart(builder::handlePart)
        val request = builder.finish()

        // TODO: make getting current account easier
        val principal = call.principal<JWTPrincipal>()!!
        val account = accounts.get(principal.subject!!.toUInt())
        checkNotNull(account)

        captures.create(
            account = account,
            front = request.frontPath,
            back = request.backPath,
            swapped = request.swapped,
            taken = request.taken,
        )

        call.respond(HttpStatusCode.NoContent)
    }
}

private class UploadRequestBuilder {
    private var frontPath: String? = null
    private var backPath: String? = null
    private var swapped: Boolean? = null
    private var taken: Instant? = null

    private val handlers =
        mapOf<String, suspend (PartData) -> Unit>(
            "back" to ::setBack,
            "front" to ::setFront,
            "swapped" to ::setSwapped,
            "taken" to ::setTaken,
        )

    suspend fun handlePart(part: PartData) {
        val handler = requireNotNull(handlers[part.name]) { "unknown field: ${part.name}" }
        handler(part)
        part.dispose()
    }

    fun finish(): UploadRequest {
        val taken = checkNotNull(taken) { "missing taken timestamp" }
        require(taken <= Clock.System.now()) { "taken timestamp cannot be in the future" }

        return UploadRequest(
            frontPath = checkNotNull(frontPath) { "no front capture uploaded" },
            backPath = checkNotNull(backPath) { "no back capture uploaded" },
            swapped = checkNotNull(swapped) { "missing swapped status" },
            taken = taken,
        )
    }

    private suspend fun setBack(item: PartData) {
        require(item is PartData.FileItem) { "back capture must be a file" }
        check(backPath == null) { "back capture already uploaded" }
        backPath = saveFile(item)
    }

    private suspend fun setFront(item: PartData) {
        require(item is PartData.FileItem) { "front capture must be a file" }
        check(frontPath == null) { "front capture already uploaded" }
        frontPath = saveFile(item)
    }

    private suspend fun setSwapped(item: PartData) {
        require(item is PartData.FormItem) { "swapped must be a form item" }

        check(swapped == null) { "value for swapped already provided" }
        swapped = item.value.toBooleanStrict()
    }

    private suspend fun setTaken(item: PartData) {
        require(item is PartData.FormItem) { "timestamp must be a form item" }

        check(taken == null) { "value for taken already provided" }
        val parsed = Instant.parse(item.value)
        require(parsed <= Clock.System.now()) { "taken timestamp cannot be in the future" }
        taken = parsed
    }

    private suspend fun saveFile(item: PartData.FileItem): String {
        // TODO: verify uploads are actually images
        // TODO: allow saving to local disk or cloud storage
        val file = File("uploads/${Uuid.random()}")
        item.provider().copyAndClose(file.writeChannel())
        return file.absolutePath
    }
}

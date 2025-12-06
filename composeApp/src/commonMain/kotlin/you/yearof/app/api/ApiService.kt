package you.yearof.app.api

import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import you.yearof.shared.api.Routes
import you.yearof.shared.api.requests.LoginRequest
import you.yearof.shared.api.requests.RegisterRequest
import you.yearof.shared.api.requests.UpdateRequest
import you.yearof.shared.api.requests.UploadRequest
import you.yearof.shared.api.responses.CapturePage
import you.yearof.shared.api.responses.CurrentUser
import you.yearof.shared.api.responses.LoginSuccess
import kotlin.time.Instant

@Single
class ApiService(
    @Provided private val httpService: HttpService,
) {
    suspend fun currentUser(): CurrentUser? =
        try {
            httpService.get(Routes.CurrentUser)
        } catch (_: UnauthorizedException) {
            null
        }

    suspend fun register(
        displayName: String,
        username: String,
        password: String,
    ) {
        val response: LoginSuccess = httpService.post(Routes.Register, RegisterRequest(displayName, username, password))
        // TODO: replace with confirmation stuff once sorted
        httpService.setTokens(response.accessToken, response.refreshToken)
    }

    suspend fun login(
        username: String,
        password: String,
    ) {
        val response: LoginSuccess = httpService.post(Routes.Login, LoginRequest(username, password))
        httpService.setTokens(response.accessToken, response.refreshToken)
    }

    suspend fun authenticated(): Boolean = httpService.hasTokens()

    suspend fun logout() = httpService.clearTokens()

    suspend fun updateAccount(
        displayName: String? = null,
        username: String? = null,
    ): CurrentUser = httpService.patch(Routes.CurrentUser, UpdateRequest(displayName, username))

    suspend fun uploadCapture(
        front: Path,
        back: Path,
        swapped: Boolean,
        at: Instant,
        onUpload: ((Long, Long?) -> Unit)? = null,
    ) {
        val request = UploadRequest(frontPath = front, backPath = back, swapped = swapped, taken = at)
        httpService.client.post(Routes.Captures()) {
            setBody(MultiPartFormDataContent(parts = request.toFormData()))
            onUpload(onUpload)
        }
    }

    suspend fun allCaptures(
        limit: Int = 20,
        cursor: String? = null,
    ): CapturePage = httpService.get(Routes.Captures.List(limit, cursor))
}

private fun UploadRequest.toFormData(): List<PartData> =
    formData {
        append("swapped", swapped)
        append("taken", taken.toString())
        appendFile("front", SystemFileSystem, frontPath)
        appendFile("back", SystemFileSystem, backPath)
    }

private fun FormBuilder.appendFile(
    key: String,
    fs: FileSystem,
    path: Path,
) {
    append(
        key = key,
        value = fileInputProvider(fs, path),
        headers =
            Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "filename=${path.name}")
            },
    )
}

private fun fileInputProvider(
    fs: FileSystem,
    path: Path,
): InputProvider {
    val meta = fs.metadataOrNull(path)
    return InputProvider(size = meta?.size) { fs.source(path).buffered() }
}

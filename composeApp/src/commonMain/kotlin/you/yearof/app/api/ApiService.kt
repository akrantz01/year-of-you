package you.yearof.app.api

import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import you.yearof.shared.api.requests.LoginRequest
import you.yearof.shared.api.requests.RegisterRequest
import you.yearof.shared.api.requests.UploadRequest
import you.yearof.shared.api.responses.CapturePage
import you.yearof.shared.api.responses.CurrentUser
import you.yearof.shared.api.responses.LoginSuccess
import you.yearof.shared.api.Routes
import kotlin.time.Instant

@Single
class ApiService(
    @Provided private val httpService: HttpService,
) {
    // TODO: probably need to handle http errors everywhere :(
    suspend fun currentUser(): CurrentUser? {
        val response = httpService.client.get(Routes.CurrentUser)
        return if (response.status == HttpStatusCode.Unauthorized) null
        else response.body()
    }

    suspend fun register(displayName: String, username: String, password: String): CurrentUser =
        httpService.post(Routes.Register, RegisterRequest(displayName, username, password))

    suspend fun login(username: String, password: String) {
        val response: LoginSuccess = httpService.post(Routes.Login, LoginRequest(username, password))
        httpService.setTokens(response.accessToken, response.refreshToken)
    }

    suspend fun authenticated(): Boolean = httpService.hasTokens()

    suspend fun logout() = httpService.clearTokens()

    suspend fun uploadCapture(front: Path, back: Path, swapped: Boolean, at: Instant, onUpload: ((Long, Long?) -> Unit)? = null) {
        val request = UploadRequest(frontPath = front, backPath = back, swapped = swapped, taken = at)
        val response = httpService.client.post(Routes.Captures()) {
            setBody(MultiPartFormDataContent(parts = request.toFormData()))
            onUpload(onUpload)
        }
        check(response.status == HttpStatusCode.NoContent)
    }

    suspend fun allCaptures(limit: Int = 20, cursor: String? = null): CapturePage = httpService.get(Routes.Captures.List(limit, cursor))
}

private fun UploadRequest.toFormData(): List<PartData> = formData {
    append("swapped", swapped)
    append("taken", taken.toString())
    appendFile("front", SystemFileSystem, frontPath)
    appendFile("back", SystemFileSystem, backPath)
}

private fun FormBuilder.appendFile(key: String, fs: FileSystem, path: Path) {
    append(
        key = key,
        value = fileInputProvider(fs, path),
        headers = Headers.build {
            append(HttpHeaders.ContentType, "image/jpeg")
            append(HttpHeaders.ContentDisposition, "filename=${path.name}")
        }
    )
}

private fun fileInputProvider(fs: FileSystem, path: Path): InputProvider {
    val meta = fs.metadataOrNull(path)
    return InputProvider(size = meta?.size) { fs.source(path).buffered() }
}

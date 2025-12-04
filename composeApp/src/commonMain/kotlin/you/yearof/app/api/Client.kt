package you.yearof.app.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.FileSystem
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import you.yearof.app.api.requests.LoginRequest
import you.yearof.app.api.requests.RefreshRequest
import you.yearof.app.api.requests.RegisterRequest
import you.yearof.app.api.requests.UploadRequest
import you.yearof.app.api.responses.CapturePage
import you.yearof.app.api.responses.CurrentUser
import you.yearof.app.api.responses.LoginSuccess
import you.yearof.app.api.responses.RefreshSuccess
import you.yearof.app.util.Log
import you.yearof.app.util.SecureStorage
import kotlin.time.Instant

private const val AccessTokenKey = "access-token"
private const val RefreshTokenKey = "refresh-token"

internal expect val engine: HttpClientEngineFactory<HttpClientEngineConfig>

@Single
class Client(
    @Provided private val secureStorage: SecureStorage,
) {
    private val inner =
        HttpClient(engine) {
            install(ContentEncoding)
            install(ContentNegotiation) {
                json()
            }

            install(Resources)

            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) = Log.info("ApiClient", message)
                    }
                level = LogLevel.HEADERS
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }

            install(Auth) {
                bearer {
                    realm = DefaultRealm
                    loadTokens {
                        val accessToken = secureStorage.get(AccessTokenKey)
                        val refreshToken = secureStorage.get(RefreshTokenKey)
                        if (accessToken != null && refreshToken != null) BearerTokens(accessToken, refreshToken)
                        else null
                    }
                    refreshTokens {
                        val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                        val response = client.post(Routes.Refresh) {
                            markAsRefreshTokenRequest()
                            contentType(ContentType.Application.Json)
                            setBody(RefreshRequest(token = refreshToken))
                        }

                        if (response.status.isSuccess()) {
                            val tokens = response.body<RefreshSuccess>()
                            setTokens(tokens.accessToken, tokens.refreshToken)
                            BearerTokens(tokens.accessToken, tokens.refreshToken)
                        } else {
                            null
                        }
                    }
                    sendWithoutRequest {
                        // TODO: create proper filter
                        true
                    }
                }
            }

            addDefaultResponseValidation()
            defaultRequest {
                // TODO: pull from preferences or a mutable flow or something
                url("https://10.0.0.33:8443")
            }
        }

    // TODO: probably need to handle http errors everywhere :(
    suspend fun currentUser(): CurrentUser? {
        val response = inner.get(Routes.CurrentUser)
        return if (response.status == HttpStatusCode.Unauthorized) null
        else response.body()
    }

    suspend fun register(displayName: String, username: String, password: String): CurrentUser =
        post(Routes.Register, RegisterRequest(displayName, username, password))

    suspend fun login(username: String, password: String) {
        val response: LoginSuccess = post(Routes.Login, LoginRequest(username, password))
        setTokens(response.accessToken, response.refreshToken)
    }

    suspend fun logout() {
        secureStorage.clear(AccessTokenKey)
        secureStorage.clear(RefreshTokenKey)
    }

    suspend fun uploadCapture(front: Path, back: Path, swapped: Boolean, at: Instant, onUpload: ((Long, Long?) -> Unit)? = null) {
        val request = UploadRequest(frontPath = front, backPath = back, swapped = swapped, taken = at)
        val response = inner.post(Routes.Captures) {
            setBody(MultiPartFormDataContent(parts = request.toFormData()))
            onUpload(onUpload)
        }
        check(response.status == HttpStatusCode.NoContent)
    }

    suspend fun allCaptures(limit: Int = 20, cursor: String? = null): CapturePage = get(Routes.Captures(limit, cursor))

    private suspend fun setTokens(access: String, refresh: String?) {
        secureStorage.put(AccessTokenKey, access)
        refresh?.let { secureStorage.put(RefreshTokenKey, it) }
    }

    private suspend inline fun <reified Route : Any, reified Response> get(route: Route): Response =
        inner.get(route).body<Response>()

    private suspend inline fun <reified Route: Any, reified Request, reified Response> post(route: Route, body: Request): Response =
        inner.post(route) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<Response>()
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

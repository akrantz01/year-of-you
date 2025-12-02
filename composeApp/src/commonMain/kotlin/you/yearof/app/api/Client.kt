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
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import you.yearof.app.api.requests.LoginRequest
import you.yearof.app.api.requests.RegisterRequest
import you.yearof.app.api.responses.CurrentUser
import you.yearof.app.api.responses.LoginSuccess
import you.yearof.app.api.responses.RefreshSuccess
import you.yearof.app.util.Log
import you.yearof.app.util.SecureStorage

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

                        val newTokens = client.post(Routes.Refresh) {
                            header(HttpHeaders.Authorization, "Bearer $refreshToken")
                            markAsRefreshTokenRequest()
                        }.body<RefreshSuccess>()
                        setTokens(newTokens.accessToken, newTokens.refreshToken)

                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
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
                url("http://10.0.0.33:8080")
            }
        }

    suspend fun currentUser(): CurrentUser? = get(Routes.CurrentUser)

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

    // TODO: handle uploading captures

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

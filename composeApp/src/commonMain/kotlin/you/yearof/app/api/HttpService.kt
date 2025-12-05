package you.yearof.app.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import you.yearof.shared.api.requests.RefreshRequest
import you.yearof.shared.api.responses.RefreshSuccess
import you.yearof.app.util.Log
import you.yearof.app.util.Preferences
import you.yearof.app.util.SecureStorage
import you.yearof.shared.api.DefaultRealm
import you.yearof.shared.api.Routes

private const val AccessTokenKey = "access-token"
private const val RefreshTokenKey = "refresh-token"

internal expect val engine: HttpClientEngineFactory<HttpClientEngineConfig>

@Single
class HttpService(
    private val preferences: Preferences,
    @Provided private val secureStorage: SecureStorage,
) {
    val client =
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

            install(UrlPlugin) {
                preferencesStore = preferences
            }

            addDefaultResponseValidation()
        }

    suspend fun hasTokens(): Boolean = secureStorage.has(AccessTokenKey) && secureStorage.has(RefreshTokenKey)

    suspend fun setTokens(access: String, refresh: String?) {
        secureStorage.put(AccessTokenKey, access)
        refresh?.let { secureStorage.put(RefreshTokenKey, it) }
    }

    suspend fun clearTokens() {
        secureStorage.clear(AccessTokenKey)
        secureStorage.clear(RefreshTokenKey)
    }

    suspend inline fun <reified Route : Any, reified Response> get(route: Route): Response =
        client.get(route).body<Response>()

    suspend inline fun <reified Route: Any, reified Request, reified Response> post(route: Route, body: Request): Response =
        client.post(route) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<Response>()

    suspend inline fun <reified Route: Any, reified Request, reified Response> patch(route: Route, body: Request): Response =
        client.patch(route) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<Response>()
}

private class UrlPluginConfig {
    lateinit var preferencesStore: Preferences
}

private val UrlPlugin = createClientPlugin(name = "url", ::UrlPluginConfig) {
    val preferences = pluginConfig.preferencesStore

    onRequest { request, _ ->
        val baseUrl = preferences.urlParsed.first()
        request.url {
            protocol = baseUrl.protocol
            host = baseUrl.host
            port = baseUrl.port
            encodedPathSegments = (baseUrl.segments + request.url.pathSegments).map { it.encodeURLPath() }
        }
    }
}

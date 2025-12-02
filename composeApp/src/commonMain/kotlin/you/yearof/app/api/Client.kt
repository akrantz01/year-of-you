package you.yearof.app.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.annotation.Single
import you.yearof.app.util.Log

internal expect val engine: HttpClientEngineFactory<HttpClientEngineConfig>

@Single
class Client {
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
                        // TODO: fetch from secure storage
                        BearerTokens("", "")
                    }
                    refreshTokens {
                        if (oldTokens == null) return@refreshTokens null

                        // TODO: send request to /auth/refresh
                        BearerTokens("", "")
                    }
                }
            }
        }

    // TODO: expose per-route methods
}

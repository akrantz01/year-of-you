package you.yearof.app.api

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual val engine: HttpClientEngineFactory<HttpClientEngineConfig> = OkHttp

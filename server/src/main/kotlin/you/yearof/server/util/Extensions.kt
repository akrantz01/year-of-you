package you.yearof.server.util

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.resources.href
import io.ktor.server.plugins.origin
import io.ktor.server.resources.Resources
import io.ktor.server.routing.RoutingContext
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import java.sql.SQLException

inline fun <reified T : Any> RoutingContext.href(resource: T): String {
    val origin = call.request.origin
    val builder =
        URLBuilder(
            protocol = checkNotNull(URLProtocol.byName[origin.scheme]) { "unknown protocol ${origin.scheme}" },
            host = origin.serverHost,
            port = origin.serverPort,
        )
    href(
        resourcesFormat = call.route.plugin(Resources).resourcesFormat,
        resource = resource,
        urlBuilder = builder,
    )
    return builder.toString()
}

val ExposedSQLException.isUniqueConstraintViolation: Boolean
    get() = sqlState.startsWith("23")

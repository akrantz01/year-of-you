package you.yearof.app.permissions.helpers

import you.yearof.app.permissions.PermissionStatus

internal interface PermissionHelper {
    fun request(onResult: (Boolean) -> Unit)

    fun read(onResult: (PermissionStatus) -> Unit)
}

internal fun PermissionHelper.handleRequest(
    onResult: (Boolean) -> Unit,
    launchRequest: () -> Unit,
) {
    read { status ->
        when (status) {
            PermissionStatus.Granted -> onResult(true)
            else -> launchRequest()
        }
    }
}

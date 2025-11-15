package you.yearof.app.permissions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Represents a lifecycle-safe handle to a single permission. Unlike [PermissionState], which can
 * only be read inside a composable, this interface exposes the permission status as a cold
 * [StateFlow] so that view models or other coordinators can consume it without being tied to
 * composition.
 */
interface PermissionHandle {
    val permission: Permission
    val status: StateFlow<PermissionStatus>

    fun request()
}

/**
 * Metadata for a permission requirement that can be composed into flows such as onboarding.
 *
 * @param handle concrete handle for the permission
 * @param optional whether the flow can proceed without ever granting the permission (for example,
 *   notifications might be opt-in whereas camera is required)
 */
data class PermissionRequirement(
    val handle: PermissionHandle,
    val optional: Boolean = false,
)

/**
 * Snapshot of all known permissions at a point in time. This allows consumers to reason about the
 * entire permission surface (for example, to decide which screen to show during onboarding) without
 * having to subscribe to each handle manually.
 */
data class PermissionsSnapshot(
    val statuses: Map<Permission, PermissionStatus>,
) {
    fun isGranted(permission: Permission): Boolean =
        statuses[permission] == PermissionStatus.Granted

    fun allMandatoryGranted(optionalPermissions: Set<Permission> = emptySet()): Boolean =
        statuses.all { (permission, status) ->
            status == PermissionStatus.Granted || permission in optionalPermissions
        }
}

/**
 * Coordinates multiple permission requirements and exposes convenient helpers to request specific
 * permissions or determine which requirement should be addressed next.
 *
 * This abstraction is UI-agnostic: it can live inside a shared view model or be injected into
 * feature modules, while the platform-specific [PermissionHandle] implementations bridge back to
 * the actual permission APIs (Android, iOS, etc.).
 */
class PermissionsCoordinator(
    scope: CoroutineScope,
    requirements: List<PermissionRequirement>,
) {
    private val byPermission: Map<Permission, PermissionRequirement> =
        requirements.associateBy { it.handle.permission }

    private val statusFlows = requirements.map { requirement ->
        requirement.handle.status.map { requirement.handle.permission to it }
    }

    val snapshot: StateFlow<PermissionsSnapshot> =
        if (statusFlows.isEmpty()) {
            MutableStateFlow(PermissionsSnapshot(emptyMap()))
        } else {
            combine(statusFlows) { entries ->
                PermissionsSnapshot(entries.toMap())
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue =
                    PermissionsSnapshot(
                        requirements.associate { it.handle.permission to PermissionStatus.Loading },
                    ),
            )
        }

    fun statusOf(permission: Permission): StateFlow<PermissionStatus>? =
        byPermission[permission]?.handle?.status

    fun request(permission: Permission) {
        byPermission[permission]?.handle?.request()
    }

    /**
     * Returns the first permission (based on [orderedPermissions]) that is required but not yet
     * granted. Optional permissions can either be skipped entirely or treated as required by
     * passing [includeOptional] = true.
     */
    fun nextBlockingPermission(
        orderedPermissions: List<Permission>,
        includeOptional: Boolean = false,
    ): Permission? {
        val snapshotValue = snapshot.value
        return orderedPermissions.firstOrNull { permission ->
            val requirement = byPermission[permission] ?: return@firstOrNull false
            if (!includeOptional && requirement.optional) return@firstOrNull false
            snapshotValue.statuses[permission] != PermissionStatus.Granted
        }
    }
}

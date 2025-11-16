package you.yearof.app.permissions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PermissionRequirement(
    val permission: Permission,
    val optional: Boolean = false,
)

data class PermissionsSnapshot(
    val statuses: Map<Permission, PermissionStatus>,
) {
    fun isGranted(permission: Permission): Boolean = statuses[permission] == PermissionStatus.Granted

    fun allMandatoryGranted(optionalPermissions: Set<Permission> = emptySet()): Boolean =
        statuses.all { (permission, status) ->
            status == PermissionStatus.Granted || permission in optionalPermissions
        }
}

class PermissionsCoordinator(
    scope: CoroutineScope,
    requirements: List<PermissionRequirement>,
) {
    init {
        if (requirements.isEmpty()) throw IllegalArgumentException("requirements can't be empty")
    }

    private val permissions = requirements.map { it.permission }
    private val optional = requirements.filter { it.optional }.map { it.permission }.toSet()

    private val statuses: Map<Permission, MutableStateFlow<PermissionStatus>> =
        requirements.associate { requirement ->
            requirement.permission to MutableStateFlow(PermissionStatus.Loading)
        }

    private val statusFlows =
        statuses.map { (permission, flow) ->
            flow.map { permission to it }
        }

    val snapshot: StateFlow<PermissionsSnapshot> =
        combine(statusFlows) { entries ->
            PermissionsSnapshot(entries.toMap())
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue =
                PermissionsSnapshot(
                    statuses.mapValues { PermissionStatus.Loading },
                ),
        )

    fun statusOf(permission: Permission): StateFlow<PermissionStatus>? = statuses[permission]

    fun update(
        permission: Permission,
        status: PermissionStatus,
    ) {
        statuses[permission]?.value = status
    }

    fun nextPermission(): Permission? {
        val snap = snapshot.value
        return permissions.firstOrNull { permission ->
            val status = snap.statuses[permission] ?: return@firstOrNull false
            if (permission in optional) {
                status != PermissionStatus.Unknown
            } else {
                status != PermissionStatus.Granted
            }
        }
    }
}

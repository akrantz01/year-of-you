package you.yearof.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import io.ktor.http.URLParserException
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.fullPath
import io.ktor.http.hostWithPortIfSpecified
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import you.yearof.app.notifications.SnackbarManager
import you.yearof.app.resources.Res
import you.yearof.app.resources.chevron_right
import you.yearof.app.util.Preferences
import you.yearof.shared.api.DefaultHost
import you.yearof.shared.api.DefaultUrl

@Composable
fun ServerSelector(modifier: Modifier = Modifier, controller: ServerSelectorController) {
    val selected by controller.currentHost.collectAsState(DefaultHost)

    val state by controller.uiState.collectAsState()
    val iconRotation by animateFloatAsState(
        targetValue = if (state.optionsExpanded) 90f else 0f,
        label = "icon-orientation",
    )

    Box(modifier = modifier) {
        TextButton(onClick = controller::expandMenu) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(16.dp).rotate(iconRotation),
                    painter = painterResource(Res.drawable.chevron_right),
                    contentDescription = "Expand menu",
                )

                Text("Accessing: $selected")
            }
        }

        DropdownMenu(expanded = state.optionsExpanded, onDismissRequest = controller::closeMenu) {
            DropdownMenuItem(
                text = { Text(DefaultHost)},
                onClick = controller::setDefault,
            )
            DropdownMenuItem(
                text = { Text("self-hosted") },
                onClick = controller::openDialog,
            )
        }

        if (state.selfHostedDialogOpen) {
            AlertDialog(
                onDismissRequest = controller::closeDialog,
                title = { Text("Self-hosted Server") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter the URL of your self-hosted server:")

                        TextField(
                            state = controller.userUrl,
                            label = { Text("URL") },
                            placeholder = { Text(DefaultUrl) },
                            isError = state.selfHostedUrlError != null,
                            supportingText = state.selfHostedUrlError?.let { { Text(it) } },
                            enabled = !state.selfHostedLoading,
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = controller::setUser, enabled = !state.selfHostedLoading) {
                        Text(if (state.selfHostedLoading) "Loading..." else "Set")
                    }
                },
                dismissButton = {
                    TextButton(onClick = controller::closeDialog, enabled = !state.selfHostedLoading) {
                        Text("Nevermind")
                    }
                }
            )
        }
    }
}

data class ServerSelectorState(
    val optionsExpanded: Boolean = false,
    val selfHostedDialogOpen: Boolean = false,
    val selfHostedUrlError: String? = null,
    val selfHostedLoading: Boolean = false,
)

class ServerSelectorController(
    private val scope: CoroutineScope,
    private val preferences: Preferences,
    private val snackbarManager: SnackbarManager,
) {
    private val _uiState = MutableStateFlow(ServerSelectorState())
    val uiState = _uiState.asStateFlow()

    val current = preferences.url.stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = DefaultUrl)
    val currentHost = current.map {
        val url = Url(it)
        "${url.hostWithPortIfSpecified}${url.fullPath}"
    }

    val userUrl = TextFieldState()

    init {
        scope.launch {
            current.collectLatest { currentUrl ->
                if (currentUrl != DefaultUrl) {
                    userUrl.setTextAndPlaceCursorAtEnd(currentUrl)
                }
            }
        }
    }

    fun expandMenu() {
        _uiState.update { it.copy(optionsExpanded = true) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(optionsExpanded = false) }
    }

    fun openDialog() {
        _uiState.update { it.copy(selfHostedDialogOpen = true) }
    }

    fun closeDialog() {
        _uiState.update { it.copy(optionsExpanded = false, selfHostedDialogOpen = false, selfHostedUrlError = null) }
    }

    fun setDefault() = scope.launch {
        preferences.setUrl(DefaultUrl)
        snackbarManager.success("Server updated to $DefaultUrl")
        closeMenu()
    }

    fun setUser() = scope.launch {
        _uiState.update { it.copy(selfHostedLoading = true) }

        try {
            doSetUser()
        } finally {
            _uiState.update { it.copy(selfHostedLoading = false) }
        }
    }

    private suspend fun doSetUser() {
        val provided = userUrl.text.toString().trim()

        val validated = try {
            parseUrl(provided)
        } catch (e: IllegalArgumentException) {
            _uiState.update { it.copy(selfHostedUrlError = e.message ?: "invalid URL") }
            return
        }

        // TODO: verify URL is reachable
        preferences.setUrl(validated)
        snackbarManager.success("Server updated to $validated")
        closeDialog()
    }

    private fun parseUrl(raw: String): String {
        require(raw.isNotBlank()) { "URL must not be empty" }

        val parsed = try {
            Url(raw)
        } catch (_: URLParserException) {
            throw IllegalArgumentException("invalid URL format")
        }

        require(parsed.protocol == URLProtocol.HTTPS) { "only HTTPS URLs are supported" }

        return parsed.toString()
    }
}

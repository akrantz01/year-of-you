package you.yearof.app.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import org.jetbrains.compose.resources.painterResource
import you.yearof.app.resources.Res
import you.yearof.app.resources.check
import you.yearof.app.resources.info
import you.yearof.app.resources.xmark

data class NotificationSnackbarVisuals(
    override val message: String,
    val kind: SnackbarKind,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

@Composable
fun BindSnackbarHost(
    snackbarManager: SnackbarManager,
    hostState: SnackbarHostState,
) {
    LaunchedEffect(snackbarManager, hostState) {
        snackbarManager.observe().collect { message ->
            hostState.showSnackbar(
                visuals =
                    NotificationSnackbarVisuals(
                        message = message.text,
                        kind = message.kind,
                        duration = message.duration,
                    ),
            )
        }
    }
}

@Composable
fun NotificationSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        val visuals = data.visuals as? NotificationSnackbarVisuals
        AppSnackbar(data = data, kind = visuals?.kind ?: SnackbarKind.Info)
    }
}

@Composable
private fun AppSnackbar(
    data: SnackbarData,
    kind: SnackbarKind,
) {
    val colors = snackbarColors(kind)
    Snackbar(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        containerColor = colors.container,
        contentColor = colors.content,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                modifier = Modifier.size(12.dp),
                painter = painterResource(when (kind) {
                    SnackbarKind.Success -> Res.drawable.check
                    SnackbarKind.Error -> Res.drawable.xmark
                    SnackbarKind.Info -> Res.drawable.info
                }),
                contentDescription = null,
                tint = colors.content,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = data.visuals.message)
        }
    }
}

private data class SnackbarColors(
    val container: Color,
    val content: Color,
)

@Composable
private fun snackbarColors(kind: SnackbarKind): SnackbarColors {
    val colorScheme = MaterialTheme.colorScheme
    val darkTheme = isSystemInDarkTheme()
    return when (kind) {
        SnackbarKind.Success ->
            SnackbarColors(
                container = if (darkTheme) Color(0xFF1B5E20) else Color(0xFFE6F4EA),
                content = if (darkTheme) Color(0xFFE6F4EA) else Color(0xFF1B5E20),
            )
        SnackbarKind.Error ->
            SnackbarColors(
                container = colorScheme.errorContainer,
                content = colorScheme.onErrorContainer,
            )
        SnackbarKind.Info ->
            SnackbarColors(
                container = colorScheme.primaryContainer,
                content = colorScheme.onPrimaryContainer,
            )
    }
}

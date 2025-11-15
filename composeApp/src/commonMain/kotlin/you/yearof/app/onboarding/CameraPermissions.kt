package you.yearof.app.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import you.yearof.app.permissions.PermissionStatus
import you.yearof.app.util.rememberSettingsAccess

@Composable
fun CameraPermissions(
    status: PermissionStatus,
    onRequest: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = rememberSettingsAccess()
    LaunchedEffect(status) {
        if (status == PermissionStatus.Granted) onContinue()
    }

    Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Allow camera access",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            Text(
                text = "Please provide access to your camera so we can capture shots of your days whenever you want.",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (status == PermissionStatus.PermanentlyDenied) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = settings::open,
                ) {
                    Text("Open Settings")
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequest,
                ) {
                    Text("Enable camera")
                }
            }
        }
    }
}

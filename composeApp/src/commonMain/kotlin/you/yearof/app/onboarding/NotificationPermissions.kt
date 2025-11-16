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

@Composable
fun NotificationPermissions(
    status: PermissionStatus,
    onRequest: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(status) {
        if (status != PermissionStatus.Loading && status != PermissionStatus.Unknown) onContinue()
    }

    Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Allow notifications?",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            Text(
                text = "We'll send you notifications at most once a day reminding you to take a picture at a semi-random time.",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onRequest,
            ) {
                Text("Enable notifications")
            }
        }
    }
}

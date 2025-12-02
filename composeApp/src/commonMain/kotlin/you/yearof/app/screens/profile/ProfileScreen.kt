package you.yearof.app.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.circle_user
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel = koinViewModel()) {
    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            onClick = viewModel::showProfile,
        ) {
            // TODO: distinguish between logged in and logged out
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    modifier = Modifier.size(80.dp),
                    painter = painterResource(Res.drawable.circle_user),
                    contentDescription = "Empty profile",
                )
                Column {
                    Text(text = "Share with friends!", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Sign in or create an account to share your captures with friends!", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

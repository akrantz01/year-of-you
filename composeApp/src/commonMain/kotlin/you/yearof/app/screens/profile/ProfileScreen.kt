package you.yearof.app.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.chevron_right
import app.composeapp.generated.resources.circle_user
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.api.AuthenticationState
import you.yearof.app.ui.SkeletonText

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel = koinViewModel()) {
    val authState by viewModel.authState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        ProfileCard(
            state = authState,
            onClickAuthenticated = viewModel::logout,
            onClickUnauthenticated = viewModel::showProfile,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProfileCard(
    state: AuthenticationState,
    onClickUnauthenticated: () -> Unit,
    onClickAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is AuthenticationState.Unauthenticated -> UnauthenticatedProfileCard(
            modifier = modifier,
            onClick = onClickUnauthenticated,
        )

        is AuthenticationState.Authenticated -> AuthenticatedProfileCard(
            username = state.username,
            displayName = state.name,
            onClick = onClickAuthenticated,
            modifier = Modifier.fillMaxWidth(),
        )

        is AuthenticationState.Loading -> SkeletonProfileCard(modifier = modifier)
    }
}

@Composable
private fun UnauthenticatedProfileCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    BaseProfileCard(modifier = modifier, onClick = onClick) {
        Icon(
            modifier = Modifier.size(80.dp).align(Alignment.CenterVertically),
            painter = painterResource(Res.drawable.circle_user),
            contentDescription = "Empty profile",
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Share with friends!", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Sign in or create an account to share your captures with friends!", style = MaterialTheme.typography.bodySmall)
        }

        Icon(
            modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
            painter = painterResource(Res.drawable.chevron_right),
            contentDescription = "Sign in or register",
        )
    }
}

@Composable
private fun AuthenticatedProfileCard(
    username: String,
    displayName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initials = remember(displayName) {
        val words = displayName.split(" ")
        buildString(2) {
            val first = words.find { it.isNotBlank() }!!
            append(first.trim().first())

            val last = words.findLast { it.isNotBlank() }!!
            if (last != first) {
                append(last.trim().first())
            }
        }
    }

    BaseProfileCard(modifier = modifier, onClick = onClick) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = displayName, style = MaterialTheme.typography.headlineMedium)
            Text(text = username, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
        }

        Icon(
            modifier = Modifier.size(32.dp).align(Alignment.CenterVertically),
            painter = painterResource(Res.drawable.chevron_right),
            contentDescription = "Account settings",
        )
    }
}

@Composable
private fun SkeletonProfileCard(modifier: Modifier = Modifier) {
    BaseProfileCard(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp).padding(8.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SkeletonText(style = MaterialTheme.typography.headlineMedium, widthFraction = 0.6f)
            SkeletonText(style = MaterialTheme.typography.bodySmall, widthFraction = 0.4f)
        }
    }
}

@Composable
private fun BaseProfileCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit,
) {
    Card(
        modifier = modifier.padding(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            content()
        }
    }
}

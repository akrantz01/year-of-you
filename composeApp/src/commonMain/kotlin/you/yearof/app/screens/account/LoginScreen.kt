package you.yearof.app.screens.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.arrow_left
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.ui.LoadingButton
import you.yearof.app.ui.PasswordTextField
import you.yearof.app.ui.UsernameTextField

@Composable
fun LoginScreen(modifier: Modifier = Modifier, viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        IconButton(
            onClick = viewModel::onCancel,
            colors = iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(Res.drawable.arrow_left),
                contentDescription = "Back",
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Welcome back!", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))

            UsernameTextField(
                state = viewModel.usernameState,
                label = { Text("Username") },
                enabled = !state.loading
            )
            PasswordTextField(
                state = viewModel.passwordState,
                label = { Text("Password") },
                enabled = !state.loading
            )

            // TODO: allow configuring server
            Spacer(modifier = Modifier.height(16.dp))

            LoadingButton(
                text = "Login",
                onClick = viewModel::onLogin,
                enabled = viewModel.canLogin,
                loading = state.loading,
            )
        }
    }
}

package you.yearof.app.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import you.yearof.app.ui.LoadingButton
import you.yearof.app.ui.PasswordTextField
import you.yearof.app.ui.UsernameTextField

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    router: AccountRouter,
    viewModel: LoginViewModel = koinViewModel(parameters = { parametersOf(router) }),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Welcome back!", style = MaterialTheme.typography.headlineLarge)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
        }

        // TODO: allow configuring server

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            OutlinedButton(
                onClick = viewModel::onCancel,
                enabled = !state.loading,
            ) {
                Text("Cancel")
            }

            LoadingButton(
                text = "Login",
                onClick = viewModel::onLogin,
                enabled = viewModel.canLogin,
                loading = state.loading,
            )
        }

        Text(
            modifier = Modifier.clickable { viewModel.toRegister() },
            text = buildAnnotatedString {
                append("Don't have an account? ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append("Sign up!")
                }
            },
        )
    }
}

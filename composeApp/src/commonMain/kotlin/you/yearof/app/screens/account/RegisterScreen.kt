package you.yearof.app.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults.iconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.arrow_left
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.ui.LoadingButton
import you.yearof.app.ui.PasswordTextField
import you.yearof.app.ui.UsernameTextField
import you.yearof.app.util.then

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, viewModel: RegisterViewModel = koinViewModel()) {
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
            Text(text = "Create your account!", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                state = viewModel.displayNameState,
                label = { Text("What should we call you?") },
                enabled = !state.loading,
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = InputTransformation.maxLength(64),
            )
            UsernameTextField(
                state = viewModel.usernameState,
                label = { Text("Choose your handle") },
                enabled = !state.loading
            )
            PasswordTextField(
                state = viewModel.passwordState,
                label = { Text("Pick a password") },
                enabled = !state.loading,
                error = !viewModel.passwordsMatch,
            )
            PasswordTextField(
                state = viewModel.passwordConfirmationState,
                label = { Text("Re-enter your password") },
                enabled = !state.loading,
                error = !viewModel.passwordsMatch,
                supportingText = viewModel.passwordsMatch.not().then { { Text("Passwords do not match") } },
            )

            // TODO: allow configuring server
            Spacer(modifier = Modifier.height(16.dp))

            LoadingButton(
                text = "Create",
                onClick = viewModel::onRegister,
                enabled = viewModel.canRegister,
                loading = state.loading,
            )

            Text(
                modifier = Modifier.clickable { viewModel.toLogin() },
                text = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("Sign in!")
                    }
                },
            )
        }
    }
}

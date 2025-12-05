package you.yearof.app.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.resources.Res
import you.yearof.app.resources.check
import you.yearof.app.ui.UsernameTextField

@Composable
fun AccountSettingsScreen(modifier: Modifier = Modifier, viewModel: AccountSettingsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Your Account",
            style = MaterialTheme.typography.headlineLarge,
        )

        EditableField(
            modifier = Modifier.fillMaxWidth(),
            saving = state.displayNameSaving,
            onSave = viewModel::onDisplayNameSave,
        ) {
            TextField(
                state = viewModel.displayNameState,
                label = { Text("Display name") },
                enabled = !state.displayNameSaving,
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = InputTransformation.maxLength(64),
            )
        }

        EditableField(
            modifier = Modifier.fillMaxWidth(),
            saving = state.usernameSaving,
            onSave = viewModel::onUsernameSave,
        ) {
            UsernameTextField(
                state = viewModel.usernameState,
                label = { Text("Username") },
                enabled = !state.usernameSaving,
            )
        }

        OutlinedButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = viewModel::back,
            enabled = !(state.usernameSaving || state.displayNameSaving),
        ) {
            Text(text = "Back")
        }
    }
}

@Composable
private fun EditableField(
    saving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    field: @Composable () -> Unit,
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            field()

            IconButton(onClick = onSave, enabled = !saving) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.check),
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

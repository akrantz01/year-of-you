package you.yearof.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.eye
import app.composeapp.generated.resources.eye_slash
import org.jetbrains.compose.resources.painterResource

@Composable
fun PasswordTextField(
    state: TextFieldState,
    enabled: Boolean = true,
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showPassword by remember { mutableStateOf(false) }

    SecureTextField(
        modifier = modifier,
        state = state,
        textObfuscationMode = if (showPassword) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    painter = painterResource(if (showPassword) Res.drawable.eye_slash else Res.drawable.eye),
                    contentDescription = if (showPassword) "Hide password" else "Show password",
                    modifier = Modifier.requiredSize(40.dp).padding(8.dp)
                )
            }
        },
        enabled = enabled,
        label = label,
    )
}

package you.yearof.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.forEachChange
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun UsernameTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
) {
    TextField(
        modifier = modifier,
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        inputTransformation = UsernameTransformation().then(InputTransformation.maxLength(64)),
        enabled = enabled,
        label = label,
    )
}

@OptIn(ExperimentalFoundationApi::class)
private class UsernameTransformation : InputTransformation {
    override val keyboardOptions =
        KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Ascii,
        )

    override fun TextFieldBuffer.transformInput() {
        val src = asCharSequence()
        changes.forEachChange { range, _ ->
            if (!range.collapsed) {
                var changed = false
                val filtered =
                    buildString(range.length) {
                        for (i in range.min until range.max) {
                            val c = src[i]
                            val lower = c.lowercaseChar()
                            val keep = lower in 'a'..'z' || lower in '0'..'9' || lower == '_'
                            if (keep) append(lower)
                            changed = changed || lower != c || !keep
                        }
                    }

                if (changed) replace(start = range.min, end = range.max, text = filtered)
            }
        }
    }
}

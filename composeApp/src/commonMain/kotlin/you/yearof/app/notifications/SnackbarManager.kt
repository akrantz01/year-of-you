package you.yearof.app.notifications

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single

enum class SnackbarKind {
    Info,
    Success,
    Error,
}

data class SnackbarMessage(
    val text: String,
    val kind: SnackbarKind = SnackbarKind.Info,
    val duration: SnackbarDuration = SnackbarDuration.Short,
)

@Single
class SnackbarManager {
    private val channel = Channel<SnackbarMessage>(capacity = Channel.BUFFERED)

    suspend fun show(message: SnackbarMessage) {
        channel.send(message)
    }

    suspend fun info(
        text: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        show(SnackbarMessage(text = text, kind = SnackbarKind.Info, duration = duration))
    }

    suspend fun success(
        text: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        show(SnackbarMessage(text = text, kind = SnackbarKind.Success, duration = duration))
    }

    suspend fun error(
        text: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        show(SnackbarMessage(text = text, kind = SnackbarKind.Error, duration = duration))
    }

    fun observe(): Flow<SnackbarMessage> = channel.receiveAsFlow()
}

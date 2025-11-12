package you.yearof.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import you.yearof.app.camera.CameraPreview
import you.yearof.app.camera.rememberCameraController

@Composable
fun CaptureScreen() {
    val scope = rememberCoroutineScope()

    val controller = rememberCameraController()
    val isReady by controller.isReady.collectAsState(false)

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            controller = controller,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    enabled = isReady,
                    onClick = {
                        scope.launch {
                            controller.updateConfiguration { config ->
                                config.copy(position = config.position.opposite())
                            }
                        }
                    }
                ) {
                    Text("Switch")
                }

                Button(
                    enabled = isReady,
                    onClick = {
                        scope.launch {
                            controller.capture()
                        }
                    }
                ) {
                    Text("Capture")
                }
            }
        }
    }
}

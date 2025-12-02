package you.yearof.app.screens.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.composeapp.generated.resources.Res
import app.composeapp.generated.resources.bolt
import app.composeapp.generated.resources.bolt_auto
import app.composeapp.generated.resources.bolt_slash
import app.composeapp.generated.resources.camera_rotate
import app.composeapp.generated.resources.circle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import you.yearof.app.camera.CameraController
import you.yearof.app.camera.CameraPosition
import you.yearof.app.camera.CameraPreview
import you.yearof.app.camera.FlashMode
import you.yearof.app.camera.rememberCameraController
import you.yearof.app.dto.CompletedCapture

@Composable
fun CaptureScreen(
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = koinViewModel(),
) {
    val controller = rememberCameraController()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val topPadding = 10.dp
        val bottomPadding = 32.dp
        val controlsMinHeight = 120.dp
        val previewMaxHeight = (maxHeight - controlsMinHeight - topPadding - bottomPadding).coerceAtLeast(0.dp)

        Column(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding)) {
            Spacer(modifier = Modifier.height(topPadding))

            CameraPreview(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = previewMaxHeight)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                controller = controller,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = controlsMinHeight),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    SwapPositionButton(controller = controller)
                    CaptureButton(controller = controller, onCaptureComplete = viewModel::onCaptureComplete)
                    FlashToggleButton(controller = controller)
                }
            }
        }
    }
}

@Composable
fun CaptureButton(
    controller: CameraController,
    onCaptureComplete: (CompletedCapture) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val isReady by controller.isReady.collectAsState(false)

    FilledIconButton(
        modifier = modifier.size(72.dp),
        enabled = isReady,
        onClick = {
            scope.launch {
                val config = controller.configuration.value
                val images = controller.capture()
                onCaptureComplete(
                    CompletedCapture(
                        swapped = config.position == CameraPosition.Front,
                        frontPath = checkNotNull(images[CameraPosition.Front]),
                        backPath = checkNotNull(images[CameraPosition.Back]),
                    ),
                )
            }
        },
    ) {
        Icon(
            modifier = Modifier.size(68.dp),
            painter = painterResource(Res.drawable.circle),
            contentDescription = "Capture",
        )
    }
}

@Composable
fun SwapPositionButton(
    controller: CameraController,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val isReady by controller.isReady.collectAsState(false)

    FilledIconButton(
        modifier = modifier,
        enabled = isReady,
        onClick = {
            scope.launch {
                controller.updateConfiguration { config ->
                    config.copy(position = config.position.opposite())
                }
            }
        },
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(Res.drawable.camera_rotate),
            contentDescription = "Rotate camera",
        )
    }
}

@Composable
fun FlashToggleButton(
    controller: CameraController,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val configuration by controller.configuration.collectAsState()
    val isReady by controller.isReady.collectAsState(false)

    FilledIconButton(
        modifier = modifier,
        enabled = isReady,
        onClick = {
            scope.launch {
                controller.updateConfiguration { config ->
                    config.copy(flashMode = config.flashMode.next())
                }
            }
        },
    ) {
        val resource =
            when (configuration.flashMode) {
                FlashMode.On -> Res.drawable.bolt
                FlashMode.Off -> Res.drawable.bolt_slash
                FlashMode.Auto -> Res.drawable.bolt_auto
            }

        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(resource),
            contentDescription = "Automatic flash",
        )
    }
}

package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.nsk.kstatemachine.event.DataEvent
import ru.nsk.kstatemachine.event.Event
import ru.nsk.kstatemachine.event.defaultDataExtractor
import ru.nsk.kstatemachine.state.*
import ru.nsk.kstatemachine.statemachine.StateMachine
import ru.nsk.kstatemachine.statemachine.createStateMachine

@Composable
fun rememberCameraController(): CameraController {
    val scope = rememberCoroutineScope()
    val camera = rememberCamera()

    DisposableEffect(Unit) {
        val job = scope.launch { camera.attach() }

        onDispose {
            job.cancel()
            camera.detach()
        }
    }

    return remember { CameraController(camera, scope) }
}

class CameraController(
    internal val camera: Camera,
    private val scope: CoroutineScope
) {
    private var machine: StateMachine? = null
    private var lock = Mutex()

    val isReady = camera.isReady.asStateFlow()

    private suspend fun <T> withLock(block: suspend () -> T) = lock.withLock { block() }

    suspend fun updateConfiguration(update: (CameraConfiguration) -> CameraConfiguration) = withLock { camera.updateConfiguration(update) }

    suspend fun capture(): Map<CameraPosition, Photo> = withLock {
        val deferred = CompletableDeferred<Map<CameraPosition, Photo>>()

        val stateMachine = stateMachine()
        stateMachine.processEvent(CaptureEvent.Start(CaptureSession(
            configuration = camera.configuration.value,
            deferred = deferred,
        )))

        deferred.await()
    }

    private suspend fun stateMachine(): StateMachine {
        val existing = machine
        if (existing != null) return existing

        val created = createStateMachine(scope, name = "CaptureWorkflow") {
            val workflow = this@CameraController
            val sm = this@createStateMachine

            addInitialState(CaptureState.Idle) {
                dataTransition<CaptureEvent.Start, CaptureSession> {
                    targetState = CaptureState.LensPreparing
                }
            }

            addState(CaptureState.LensPreparing) {
                onEntry {
                    val session = data
                    try {
                        workflow.camera.waitReady(requireUnready = (session.stage == LensStage.Second))
                        sm.processEvent(CaptureEvent.LensPrepared(session))
                    } catch (t: Throwable) {
                        sm.processEvent(CaptureEvent.Failed(t))
                    }
                }

                dataTransition<CaptureEvent.LensPrepared, CaptureSession> {
                    targetState = CaptureState.LensCapturing
                }
            }

            addState(CaptureState.LensCapturing) {
                onEntry {
                    val session = data
                    try {
                        val photo = workflow.camera.captureImage()
                        val updated = session.copy(photos = session.photos.apply {
                            put(session.position, photo)
                        })
                        sm.processEvent(CaptureEvent.PhotoCaptured(photo, updated))
                    } catch (t: Throwable) {
                        sm.processEvent(CaptureEvent.Failed(t))
                    }
                }

                dataTransition<CaptureEvent.PhotoCaptured, CaptureSession> {
                    guard = { event.data.stage == LensStage.First }
                    targetState = CaptureState.SwitchingLens
                }

                dataTransition<CaptureEvent.PhotoCaptured, CaptureSession> {
                    guard = { event.data.stage == LensStage.Second }
                    targetState = CaptureState.Finalizing
                }
            }

            addState(CaptureState.SwitchingLens) {
                onEntry {
                    val session = data
                    try {
                        val next = session.configuration.position.opposite()
                        workflow.camera.configuration.value = session.configuration.copy(position = next)
                        val updated = session.copy(
                            position = next,
                            stage = LensStage.Second,
                            switchedLens = true,
                        )
                        sm.processEvent(CaptureEvent.LensSwitched(updated))
                    } catch (t: Throwable) {
                        sm.processEvent(CaptureEvent.Failed(t))
                    }
                }

                dataTransition<CaptureEvent.LensSwitched, CaptureSession> {
                    targetState = CaptureState.LensPreparing
                }
            }

            addState(CaptureState.Finalizing) {
                onEntry {
                    val session = data
                    session.deferred.complete(session.photos)
                    sm.processEvent(CaptureEvent.Finalized(session))
                }

                dataTransition<CaptureEvent.Finalized, CaptureSession> {
                    targetState = CaptureState.Cleaning
                }
            }

            addState(CaptureState.Cleaning) {
                onEntry {
                    val session = data
                    val cleanup = runCatching {
                        workflow.camera.configuration.value = session.configuration
                        workflow.camera.waitReady(requireUnready = session.switchedLens)
                    }

                    if (cleanup.isFailure && !session.deferred.isCompleted) {
                        session.deferred.completeExceptionally(cleanup.exceptionOrNull()!!)
                    }

                    sm.processEvent(CaptureEvent.Cleaned(session))
                }

                transition<CaptureEvent.Cleaned> {
                    targetState = CaptureState.Idle
                }
            }

            transition<CaptureEvent.Failed> {
                targetState = CaptureState.Cleaning
            }
        }

        machine = created
        return created
    }
}

private enum class LensStage { First, Second }

private data class CaptureSession(
    val configuration: CameraConfiguration,
    val position: CameraPosition = configuration.position,
    val stage: LensStage = LensStage.First,
    val switchedLens: Boolean = false,
    val photos: LinkedHashMap<CameraPosition, Photo> = LinkedHashMap(),
    val deferred: CompletableDeferred<Map<CameraPosition, Photo>>
)

private sealed interface CaptureState : State {
    sealed class WithSession(name: String) : DefaultDataState<CaptureSession>(
        name = name,
        dataExtractor = defaultDataExtractor(),
    ), CaptureState

    data object Idle : DefaultState("Idle"), CaptureState
    data object LensPreparing : WithSession("LensPreparing")
    data object LensCapturing : WithSession("LensCapturing")
    data object SwitchingLens : WithSession("SwitchingLens")
    data object Finalizing : WithSession("Finalizing")
    data object Cleaning : WithSession("Cleaning")
}

private sealed interface CaptureEvent : Event {
    data class Start(override val data: CaptureSession) : DataEvent<CaptureSession>, CaptureEvent
    data class LensPrepared(override val data: CaptureSession) : DataEvent<CaptureSession>, CaptureEvent
    data class PhotoCaptured(val photo: Photo, override val data: CaptureSession) : DataEvent<CaptureSession>, CaptureEvent
    data class LensSwitched(override val data: CaptureSession) : DataEvent<CaptureSession>, CaptureEvent
    data class Finalized(override val data: CaptureSession) : DataEvent<CaptureSession>, CaptureEvent
    data class Failed(val throwable: Throwable) : CaptureEvent
    data class Cleaned(override val data: CaptureSession) : DataEvent<CaptureSession>, CaptureEvent
}

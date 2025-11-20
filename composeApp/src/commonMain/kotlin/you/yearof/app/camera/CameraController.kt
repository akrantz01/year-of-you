package you.yearof.app.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.nsk.kstatemachine.event.DataEvent
import ru.nsk.kstatemachine.event.Event
import ru.nsk.kstatemachine.event.defaultDataExtractor
import ru.nsk.kstatemachine.state.DefaultDataState
import ru.nsk.kstatemachine.state.DefaultState
import ru.nsk.kstatemachine.state.FinalState
import ru.nsk.kstatemachine.state.State
import ru.nsk.kstatemachine.state.addFinalState
import ru.nsk.kstatemachine.state.addInitialState
import ru.nsk.kstatemachine.state.addState
import ru.nsk.kstatemachine.state.dataTransition
import ru.nsk.kstatemachine.state.onEntry
import ru.nsk.kstatemachine.statemachine.StateMachine
import ru.nsk.kstatemachine.statemachine.createStateMachine
import ru.nsk.kstatemachine.statemachine.destroy
import ru.nsk.kstatemachine.statemachine.onTransitionComplete

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

@Stable
class CameraController(
    internal val camera: Camera,
    private val scope: CoroutineScope,
) {
    private val lock = Mutex()

    private val state = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val currentState = state.asStateFlow()
    val configuration = camera.configuration.asStateFlow()
    val isReady = currentState.map { state -> state == CaptureState.Idle || state == CaptureState.Succeeded }

    private suspend fun <T> withLock(block: suspend () -> T) = lock.withLock { block() }

    suspend fun updateConfiguration(update: (CameraConfiguration) -> CameraConfiguration) =
        withLock {
            camera.updateConfiguration(update)
        }

    suspend fun capture(): Map<CameraPosition, String> =
        withLock {
            val deferred = CompletableDeferred<Map<CameraPosition, String>>()

            state.emit(CaptureState.Idle)
            val stateMachine = stateMachine()

            try {
                stateMachine.processEvent(
                    CaptureEvent.Start(
                        CaptureSession(
                            configuration = camera.configuration.value,
                            deferred = deferred,
                        ),
                    ),
                )

                deferred.await()
            } finally {
                stateMachine.destroy()
            }
        }

    @Suppress("LongMethod")
    private suspend fun stateMachine(): StateMachine =
        createStateMachine(scope, name = "CaptureWorkflow") {
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
                        sm.processEvent(CaptureEvent.Failed(session.copy(error = t)))
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
                        val updated =
                            session.copy(
                                photos =
                                    session.photos.apply {
                                        put(session.position, photo)
                                    },
                            )
                        sm.processEvent(CaptureEvent.PhotoCaptured(photo, updated))
                    } catch (t: Throwable) {
                        sm.processEvent(CaptureEvent.Failed(session.copy(error = t)))
                    }
                }

                dataTransition<CaptureEvent.PhotoCaptured, CaptureSession> {
                    guard = { event.data.stage == LensStage.First }
                    targetState = CaptureState.SwitchingLens
                }

                dataTransition<CaptureEvent.PhotoCaptured, CaptureSession> {
                    guard = { event.data.stage == LensStage.Second }
                    targetState = CaptureState.Cleaning
                }
            }

            addState(CaptureState.SwitchingLens) {
                onEntry {
                    val session = data
                    try {
                        val next = session.configuration.position.opposite()
                        workflow.camera.configuration.value = session.configuration.copy(position = next)
                        val updated =
                            session.copy(
                                position = next,
                                stage = LensStage.Second,
                                switchedLens = true,
                            )
                        sm.processEvent(CaptureEvent.LensSwitched(updated))
                    } catch (t: Throwable) {
                        sm.processEvent(CaptureEvent.Failed(session.copy(error = t)))
                    }
                }

                dataTransition<CaptureEvent.LensSwitched, CaptureSession> {
                    targetState = CaptureState.LensPreparing
                }
            }

            addState(CaptureState.Cleaning) {
                onEntry {
                    val session = data
                    val cleanup =
                        runCatching {
                            workflow.camera.configuration.value = session.configuration
                            workflow.camera.waitReady(requireUnready = session.switchedLens)
                        }

                    val combinedError =
                        when {
                            cleanup.isSuccess -> session.error
                            session.error == null -> cleanup.exceptionOrNull()
                            else -> session.error.also { it.addSuppressed(checkNotNull(cleanup.exceptionOrNull())) }
                        }

                    val updated = session.copy(error = combinedError)
                    sm.processEvent(CaptureEvent.Cleaned(updated))
                }

                dataTransition<CaptureEvent.Cleaned, CaptureSession> {
                    guard = { event.data.error == null }
                    targetState = CaptureState.Succeeded
                }

                dataTransition<CaptureEvent.Cleaned, CaptureSession> {
                    guard = { event.data.error != null }
                    targetState = CaptureState.Failed
                }
            }

            addFinalState(CaptureState.Succeeded) {
                onEntry {
                    val session = data
                    session.deferred.complete(session.photos)
                }
            }

            addFinalState(CaptureState.Failed) {
                onEntry {
                    val session = data
                    session.deferred.completeExceptionally(checkNotNull(session.error))
                }
            }

            dataTransition<CaptureEvent.Failed, CaptureSession> {
                targetState = CaptureState.Cleaning
            }

            onTransitionComplete { states, _ ->
                check(states.size == 1) { "expected 1 active state, got $states" }
                state.emit(states.first() as CaptureState)
            }
        }
}

enum class LensStage { First, Second }

data class CaptureSession(
    val configuration: CameraConfiguration,
    val position: CameraPosition = configuration.position,
    val stage: LensStage = LensStage.First,
    val switchedLens: Boolean = false,
    val photos: LinkedHashMap<CameraPosition, String> = LinkedHashMap(),
    val error: Throwable? = null,
    val deferred: CompletableDeferred<Map<CameraPosition, String>>,
)

sealed interface CaptureState : State {
    sealed class WithSession(
        name: String,
    ) : DefaultDataState<CaptureSession>(
            name = name,
            dataExtractor = defaultDataExtractor(),
        ),
        CaptureState

    data object Idle : DefaultState("Idle"), CaptureState

    data object LensPreparing : WithSession("LensPreparing")

    data object LensCapturing : WithSession("LensCapturing")

    data object SwitchingLens : WithSession("SwitchingLens")

    data object Cleaning : WithSession("Cleaning")

    data object Succeeded : WithSession("Finished"), FinalState

    data object Failed : WithSession("Failed"), FinalState
}

private sealed interface CaptureEvent : Event {
    data class Start(
        override val data: CaptureSession,
    ) : DataEvent<CaptureSession>,
        CaptureEvent

    data class LensPrepared(
        override val data: CaptureSession,
    ) : DataEvent<CaptureSession>,
        CaptureEvent

    data class PhotoCaptured(
        val photo: String,
        override val data: CaptureSession,
    ) : DataEvent<CaptureSession>,
        CaptureEvent

    data class LensSwitched(
        override val data: CaptureSession,
    ) : DataEvent<CaptureSession>,
        CaptureEvent

    data class Failed(
        override val data: CaptureSession,
    ) : DataEvent<CaptureSession>,
        CaptureEvent

    data class Cleaned(
        override val data: CaptureSession,
    ) : DataEvent<CaptureSession>,
        CaptureEvent
}

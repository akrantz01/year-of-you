package you.yearof.server.scheduler

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.plugins.di.dependencies
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toJavaInstant

fun Application.scheduler() {
    monitor.subscribe(ApplicationStarted) {
        launchJob("clean unfinished uploads", 1.hours) {
            // TODO: implement this job
        }
    }
}

private fun Application.launchJob(
    name: String,
    every: Duration,
    minDuration: Duration = 0.minutes,
    maxDuration: Duration = 10.minutes,
    context: CoroutineDispatcher = Dispatchers.Default,
    block: suspend () -> Unit,
) {
    val executor: LockingTaskExecutor by dependencies
    val task = Runnable { runBlocking { block() } }

    launch(context) {
        while (isActive) {
            // TODO: add logging
            executor.executeWithLock(
                task,
                LockConfiguration(
                    Clock.System.now().toJavaInstant(),
                    name,
                    maxDuration.toJavaDuration(),
                    minDuration.toJavaDuration(),
                ),
            )
            delay(every)
        }
    }
}

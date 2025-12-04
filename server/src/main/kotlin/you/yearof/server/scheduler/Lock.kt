package you.yearof.server.scheduler

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor
import net.javacrumbs.shedlock.core.ExtensibleLockProvider
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import net.javacrumbs.shedlock.provider.exposed.ExposedLockProvider
import org.jetbrains.exposed.v1.jdbc.Database

fun taskExecutor(lock: ExtensibleLockProvider): LockingTaskExecutor = DefaultLockingTaskExecutor(lock)

fun databaseLock(db: Database): ExtensibleLockProvider = ExposedLockProvider(db)

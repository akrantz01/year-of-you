package you.yearof.server.database.repositories

import org.jetbrains.exposed.v1.jdbc.Database
import you.yearof.server.repositories.AccountRepository
import you.yearof.server.repositories.CaptureRepository

fun accountRepository(db: Database): AccountRepository = SqlAccountRepository(db)

fun captureRepository(db: Database): CaptureRepository = SqlCaptureRepository(db)

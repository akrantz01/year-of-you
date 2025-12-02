package you.yearof.app.database.repositories

import org.jetbrains.exposed.v1.jdbc.Database
import you.yearof.app.repositories.AccountRepository
import you.yearof.app.repositories.CaptureRepository

fun accountRepository(db: Database): AccountRepository = SqlAccountRepository(db)

fun captureRepository(db: Database): CaptureRepository = SqlCaptureRepository(db)

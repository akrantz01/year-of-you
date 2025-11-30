package you.yearof.app

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.CommandLineConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import you.yearof.app.database.Accounts
import you.yearof.app.database.createConnectionPool
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

val ScriptsDirectory = Path("server/src/main/resources/migrations")
val SlugRegex = Regex("[^a-zA-Z0-9]")
val VersionRegex = Regex("^V(?<number>[0-9]+)__")

fun main(args: Array<String>) {
    val cli = CommandLineConfig(args)

    val migrationName = prompt("Migration name? ").lowercase().replace(SlugRegex, "_")
    check(migrationName.isNotBlank()) { "A migration name is required" }

    val version = ScriptsDirectory.currentVersion() + 1
    val path = ScriptsDirectory.resolve("V${version}__$migrationName.sql")

    if (confirm("Auto-generate migration content?")) {
        path.autoGenerate(cli.environment.config)
    } else {
        path.createFile()
    }

    println("Created new migration: ${path.fileName}")
}

private fun java.nio.file.Path.autoGenerate(config: ApplicationConfig) {
    val dataSource = config.createConnectionPool()
    val db = Database.connect(dataSource)

    transaction(db) {
        MigrationUtils.generateMigrationScript(
            Accounts,
            scriptDirectory = parent.pathString,
            scriptName = nameWithoutExtension,
            withLogs = true,
        )
    }
}

private fun java.nio.file.Path.currentVersion(): Int {
    val contents = listDirectoryEntries("*.sql")
    if (contents.isEmpty()) return 0

    return contents
        .mapNotNull {
            VersionRegex.matchEntire(
                it.name,
            )
        }.mapNotNull { it.groups[0]?.value?.toIntOrNull() }
        .maxOrNull() ?: 0
}

private fun prompt(message: String): String {
    print("$message ")
    return readln().trim()
}

private fun confirm(message: String): Boolean {
    do {
        val raw = prompt("$message (Y/n): ").lowercase()
        if (raw == "y" || raw == "yes") {
            return true
        } else if (raw == "n" || raw == "no") {
            return false
        }

        println("Answer y/yes or n/no")
    } while (true)
}

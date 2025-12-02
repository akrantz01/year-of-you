plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
    id("you.yearof.build.codequality")
    id("you.yearof.build.toolchain")
}

group = "you.yearof.app"
version = "1.0.0"
application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.bouncycastle)
    implementation(libs.database.h2)
    implementation(libs.database.hikaricp)
    implementation(libs.database.mariadb)
    implementation(libs.database.mysql)
    implementation(libs.database.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.datetime)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.migration.core)
    implementation(libs.exposed.migration.jdbc)
    implementation(libs.flyway)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.flyway.database.mysql)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.di)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.network.tlsCertificates)
    implementation(libs.ktor.server.requestValidation)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.logback)
    implementation(libs.logback.apacheCommonsBridge)
    implementation(libs.shedlock.core)
    implementation(libs.shedlock.exposed)
    implementation(libs.spring.security.crypto)
    testImplementation(libs.ktor.server.test)
    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.apply {
            add("-opt-in=kotlin.time.ExperimentalTime")
            add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")
            add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
            add("-opt-in=org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi")
        }
    }
}

tasks.register<JavaExec>("generateMigration") {
    group = "application"
    description = "Generate a new database migration"

    classpath = sourceSets.getByName("main").runtimeClasspath
    mainClass = "you.yearof.app.GenerateMigrationKt"
}

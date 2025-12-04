plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
    id("you.yearof.build.codequality")
    id("you.yearof.build.toolchain")
}

kotlin {
    androidTarget()

    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.ktor.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
    }
}

android {
    namespace = "you.yearof.shared"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
}

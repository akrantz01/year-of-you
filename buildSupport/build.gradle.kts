plugins {
    `kotlin-dsl`
    alias(libs.plugins.javaGradlePlugin)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(libs.detekt)
    implementation(libs.spotless)
    implementation(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    val codequality by plugins.creating {
        id = "you.yearof.build.codequality"
        implementationClass = "you.yearof.build.CodeQualityPlugin"
    }
    val jdkToolchain by plugins.creating {
        id = "you.yearof.build.toolchain"
        implementationClass = "you.yearof.build.JdkToolchainPlugin"
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint(libs.versions.ktlint.get())
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

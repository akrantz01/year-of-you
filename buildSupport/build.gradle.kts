plugins {
    `kotlin-dsl`
    alias(libs.plugins.javaGradlePlugin)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(libs.detekt)
    implementation(libs.spotless)
}

gradlePlugin {
    val codequality by plugins.creating {
        id = "you.yearof.build.codequality"
        implementationClass = "you.yearof.build.CodeQualityPlugin"
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

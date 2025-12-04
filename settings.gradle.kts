rootProject.name = "year-of-you"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

if (JavaVersion.current() != JavaVersion.VERSION_21) {
    error("Gradle must be run on JDK 21 (current: ${System.getProperty("java.version")})")
}

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    includeBuild("buildSupport")
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":server")
include(":shared")

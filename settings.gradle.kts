pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // compose-markdown (com.github.jeziellago:compose-markdown) is published on JitPack.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SweetSpot"
include(":app")
include(":shared")
include(":wear")

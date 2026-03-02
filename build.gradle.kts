plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
    id("org.jetbrains.dokka") version "2.1.0"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

dependencies {
    dokka(project(":shared"))
    dokka(project(":app"))
    dokka(project(":wear"))
}

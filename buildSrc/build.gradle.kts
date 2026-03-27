plugins {
    `kotlin-dsl`
    idea
}

idea {
    module {
        // Mark Kotlin DSL generated sources so IntelliJ treats them as generated
        // code and skips them in most inspections
        generatedSourceDirs.addAll(files(
            "build/generated-sources/kotlin-dsl-accessors/kotlin",
            "build/generated-sources/kotlin-dsl-plugins/kotlin",
            "build/generated-sources/kotlin-dsl-external-plugin-spec-builders/kotlin"
        ))
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Keep these versions in sync with gradle/libs.versions.toml
    implementation("com.android.tools.build:gradle:9.0.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.10")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.3.10")
}

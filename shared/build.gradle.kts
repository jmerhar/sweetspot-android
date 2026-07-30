plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.kover)
}

android {
    namespace = "today.sweetspot.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    testOptions {
        // Robolectric needs merged Android resources to spin up a Context.
        unitTests.isIncludeAndroidResources = true
    }
}

kover {
    reports {
        filters {
            excludes {
                // Compiler-generated code with no meaningful logic to test: kotlinx-serialization
                // serializers / data-class members on @Serializable types, and BuildConfig.
                annotatedBy("kotlinx.serialization.Serializable")
                classes("*.BuildConfig")
            }
        }
        // NB: the CI coverage gate is `bin/quality/coverage-report.py --gate` (reads this filtered XML
        // report), not `koverVerifyDebug` — see the script's docstring and app/build.gradle.kts.
    }
}

dependencies {
    implementation(libs.core.ktx)

    // OkHttp
    implementation(libs.okhttp)

    // kotlinx-serialization
    implementation(libs.serialization.json)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.serialization.json)
    testImplementation(libs.kxml2)
    // Robolectric — for the Context-backed data classes (FilePriceCache, SettingsRepository)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core)
}

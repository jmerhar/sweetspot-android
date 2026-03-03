plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "si.merhar.sweetspot.shared"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM (needed for ApplianceIcon which uses Material Icons)
    implementation(platform(libs.compose.bom))

    implementation(libs.core.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.material.icons.extended)

    // OkHttp
    implementation(libs.okhttp)

    // kotlinx-serialization
    implementation(libs.serialization.json)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.serialization.json)
    testImplementation(libs.kxml2)
}

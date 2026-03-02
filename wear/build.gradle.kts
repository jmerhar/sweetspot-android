import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "si.merhar.sweetspot.wear"
    compileSdk = 35

    defaultConfig {
        applicationId = "si.merhar.sweetspot"
        minSdk = 30
        targetSdk = 35
        versionCode = 7
        versionName = "2.2"
    }

    signingConfigs {
        create("release") {
            val props = rootProject.file("local.properties")
            if (props.exists()) {
                val localProps = Properties().apply { load(props.inputStream()) }
                val storeFilePath = localProps.getProperty("RELEASE_STORE_FILE")
                if (storeFilePath != null) {
                    storeFile = rootProject.file(storeFilePath)
                    storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD")
                    keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS")
                    keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Shared module (data, model, util layers)
    implementation(project(":shared"))

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    // Wear Compose
    implementation("androidx.wear.compose:compose-material:1.4.1")
    implementation("androidx.wear.compose:compose-foundation:1.4.1")
    implementation("androidx.wear.compose:compose-navigation:1.4.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Wearable Data Layer
    implementation("com.google.android.gms:play-services-wearable:19.0.0")

    // Coroutines + play-services await()
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Serialization (for decoding appliance JSON from Data Layer)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}

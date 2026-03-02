import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "si.merhar.sweetspot.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "si.merhar.sweetspot"
        minSdk = 30
        targetSdk = 35
        versionCode = 8
        versionName = "2.3"
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

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // Shared module (data, model, util layers)
    implementation(project(":shared"))

    // Compose BOM
    implementation(platform(libs.compose.bom))

    // Wear Compose
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)

    implementation(libs.compose.ui)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Wearable Data Layer
    implementation(libs.play.services.wearable)

    // Coroutines + play-services await()
    implementation(libs.coroutines.play.services)

    // Serialization (for decoding appliance JSON from Data Layer)
    implementation(libs.serialization.json)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.serialization.json)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core)
    testImplementation(libs.test.ext.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.arch.core.testing)
}

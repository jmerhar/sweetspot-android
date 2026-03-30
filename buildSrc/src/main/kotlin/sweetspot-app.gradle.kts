/**
 * Convention plugin for SweetSpot app modules (phone + wear).
 *
 * Configures everything shared between :app and :wear:
 * plugins, compile/target SDK, signing, build types, ENTSO-E token,
 * Java/Kotlin compile options, Compose, and test options.
 *
 * Each module still declares its own namespace, minSdk, and dependencies.
 */

import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "today.sweetspot"
        targetSdk = 36
        versionCode = 16
        versionName = "4.1"

        // Include only IANA-recognised language codes in bundles.
        // Montenegrin (cnr) is excluded because the Play Console rejects it;
        // Montenegrin speakers fall back to Serbian (sr) or Croatian (hr).
        androidResources.localeFilters += listOf(
            "bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr",
            "hr", "hu", "it", "lt", "lv", "mk", "nb", "nl", "pl", "pt",
            "ro", "sk", "sl", "sr", "sv"
        )

        val props = rootProject.file("local.properties")
        val entsoeToken = if (props.exists()) {
            Properties().apply { load(props.inputStream()) }
                .getProperty("ENTSOE_API_TOKEN", "")
        } else ""
        buildConfigField("String", "ENTSOE_API_TOKEN", "\"$entsoeToken\"")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

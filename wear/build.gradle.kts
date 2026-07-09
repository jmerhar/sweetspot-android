plugins {
    id("sweetspot-app")
    alias(libs.plugins.kover)
}

base.archivesName = "sweetspot-wear"

kover {
    reports {
        filters {
            excludes {
                // Presentation & framework glue that unit tests can't exercise. Any real logic must
                // live outside these (e.g. in WearViewModel / WearSync) — see CLAUDE.md.
                annotatedBy("androidx.compose.runtime.Composable")   // all Wear Compose UI
                classes(
                    "*ComposableSingletons*",                        // generated Compose lambda holders
                    "*.BuildConfig",
                    "today.sweetspot.wear.WearActivity",             // Compose host / navigation
                    "today.sweetspot.wear.WearableSync",             // real Wearable Data Layer plumbing
                    "today.sweetspot.wear.WearableSync\$*",          // …and its nested lambda classes
                )
            }
        }
        // Coverage gate for :wear (after the presentation/glue exclusions above). Fails
        // `koverVerifyDebug` if line coverage drops below the bound. Currently ~95%, so 93 leaves a
        // small buffer for the lifecycle/refresh-loop framework glue while catching a real regression.
        verify {
            rule("Line coverage of :wear") {
                bound {
                    minValue = 93
                }
            }
        }
    }
}

android {
    namespace = "today.sweetspot.wear"

    defaultConfig {
        minSdk = 30
        // Offset wear versionCode so Play Console can distinguish phone and wear bundles.
        // Without this, uploading both AABs causes one to replace the other.
        versionCode = (versionCode ?: 0) + 1_000_000
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
    implementation(libs.appcompat)
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

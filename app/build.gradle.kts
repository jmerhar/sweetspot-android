plugins {
    id("sweetspot-app")
    alias(libs.plugins.kover)
}

base.archivesName = "sweetspot"

kover {
    reports {
        filters {
            excludes {
                // Presentation & framework glue that unit tests can't exercise. Any real logic must
                // live outside these (e.g. in SweetSpotViewModel / shared util) — see CLAUDE.md.
                annotatedBy("androidx.compose.runtime.Composable")   // all Compose UI
                classes(
                    "*ComposableSingletons*",                        // generated Compose lambda holders
                    "*.BuildConfig",
                    // Compose screens, components, theme. Each sub-package is listed explicitly: the
                    // `koverVerifyDebug` gate treats `*` as not crossing package dots (unlike report
                    // generation), so a single `today.sweetspot.ui.*` would leak sub-package
                    // non-@Composable code (theme/colour/config constants) into the verified number.
                    "today.sweetspot.ui.*",
                    "today.sweetspot.ui.components.*",
                    "today.sweetspot.ui.settings.*",
                    "today.sweetspot.ui.theme.*",
                    "today.sweetspot.MainActivity",                  // Compose host / navigation
                    "today.sweetspot.MainActivity\$*",
                    "today.sweetspot.MainActivityKt",                // its @Composable dialogs
                    "today.sweetspot.WearableStatsBridge",           // real Wearable Data Layer plumbing
                    "today.sweetspot.WearableStatsBridge\$*",
                    "today.sweetspot.data.billing.PlayBillingRepository",     // real Play Billing wrapper
                    "today.sweetspot.data.billing.PlayBillingRepository\$*",
                    "today.sweetspot.data.stats.HttpStatsPoster",    // real HTTP POST plumbing
                )
            }
        }
        // Coverage gate for :app. Currently ~99% line, so 97 leaves a small buffer for defensive/DI
        // lines while still catching a real regression. Verification inherits the excludes above.
        verify {
            rule("Line coverage of :app") {
                bound {
                    minValue = 97
                }
            }
        }
    }
}

android {
    namespace = "today.sweetspot"

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // Shared module (data, model, util layers)
    implementation(project(":shared"))

    // Compose BOM
    implementation(platform(libs.compose.bom))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)

    // Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.lifecycle.viewmodel.compose)

    // Wearable Data Layer (sync appliances to watch)
    implementation(libs.play.services.wearable)

    // Play Billing (one-time in-app purchase for full unlock)
    implementation(libs.billing)

    // kotlinx-serialization (used by ViewModel for Data Layer sync)
    implementation(libs.serialization.json)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.serialization.json)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core)
    testImplementation(libs.test.ext.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.arch.core.testing)

    // Instrumented testing (screenshot automation)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.screengrab)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

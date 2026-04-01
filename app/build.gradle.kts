plugins {
    id("sweetspot-app")
}

base.archivesName = "sweetspot"

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
    implementation(libs.compose.material.icons.extended)
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

    debugImplementation(libs.compose.ui.tooling)
}

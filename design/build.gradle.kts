// The needanapp design package: theme, appearance settings and building blocks in the Material 3 Expressive style.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

// coordinates for apps that include this repo as a composite build: implementation("de.needanapp:design")
group = "de.needanapp"
version = providers.gradleProperty("DESIGN_VERSION").get()

android {
    namespace = "de.needanapp.design"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 31
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.activity.compose)
    api(libs.haze)
    api(libs.haze.blur)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.materialkolor)
    // ContrastTest checks every theme for legible text
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

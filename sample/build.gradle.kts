// Demo app to try every building block and appearance setting of the design package.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.needanapp.design.sample"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        applicationId = "de.needanapp.design.sample"
        minSdk = 31
        targetSdk = 37
        versionName = providers.gradleProperty("DESIGN_VERSION").get()
        versionCode = 1
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
        localeFilters += listOf("en", "de")
    }
}

dependencies {
    implementation(project(":design"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

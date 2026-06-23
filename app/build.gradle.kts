plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // KSP is used by Room to generate the DAO/database implementation code.
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.eventpass"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.eventpass"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Compose / AndroidX core (from template) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Navigation Compose: screen-to-screen navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- ViewModel + StateFlow integration for Compose (MVVM) ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // --- Room: local persistence ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- CameraX: live camera preview + image analysis ---
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // --- ML Kit: QR / barcode recognition ---
    implementation(libs.mlkit.barcode.scanning)

    // --- ZXing: QR code generation for new attendees ---
    implementation(libs.zxing.core)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

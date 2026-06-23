// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Declared here so the app module can apply it; needed by Room's annotation processor.
    alias(libs.plugins.ksp) apply false
}

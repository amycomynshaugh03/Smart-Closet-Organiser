// This build.gradle file defines the setup and dependencies for the Android project.
// The file includes:
// 1. Plugin configurations for Android application and Kotlin.
// 2. Android-specific settings, such as the SDK versions and build types.
// 3. Dependencies for core Android libraries, testing, logging, networking, and image loading.

plugins {
    // Apply the Android application plugin
    alias(libs.plugins.android.application)
    // Apply the Kotlin Android plugin
    alias(libs.plugins.kotlin.android)
    // Apply Kotlin Parcelize plugin for Parcelable data classes
    id("kotlin-parcelize")
    // Dokka
    id("org.jetbrains.dokka") version "1.8.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    kotlin("plugin.serialization") version "1.9.21"

}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("documentation/html"))
}

android {
    // Namespace for the app (unique identifier)
    namespace = "ie.setu.project"
    // Target Android SDK version for compiling the app
    compileSdk = 35

    buildFeatures {
        // Enable view binding for easy access to views
        viewBinding = true
        compose = true
    }

    defaultConfig {
        // Application ID (package name) of the app
        applicationId = "ie.setu.project"
        // Minimum SDK version supported by the app
        minSdk = 30
        // Target SDK version for the app
        targetSdk = 35
        // Version code for the app (increased with each release)
        versionCode = 1
        // Version name for the app (can be any string like 1.0)
        versionName = "1.0"
        // Test instrumentation runner for running Android tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Configuration for the release build
        release {
            // Disable minification (Proguard/R8 optimization)
            isMinifyEnabled = false
            // Specify Proguard files for code optimization and obfuscation
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // Java compatibility settings
    compileOptions {
        // Specify Java 11 compatibility for the source code
        sourceCompatibility = JavaVersion.VERSION_11
        // Specify Java 11 compatibility for the compiled code
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Kotlin compiler options (use JVM target version 11)
    kotlinOptions {
        jvmTarget = "11"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
            force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
    }


}

dependencies {
    // AndroidX libraries for core functionality and UI components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.androidx.compose.foundation)

    // Testing dependencies for unit and UI tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Logging dependencies
    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)
    implementation(libs.timberkt)

    // Networking and HTTP dependencies (Retrofit)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Coroutines for asynchronous programming
    implementation(libs.kotlinx.coroutines.android.v143)

    // Lifecycle components for ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v241)
    implementation(libs.androidx.lifecycle.livedata.ktx.v241)

    // Image loading library (Picasso)
    implementation(libs.picasso)

    implementation(libs.gson)

    // Ktor for Open-Meteo
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // DateTime support
    implementation(libs.kotlinx.datetime)

    // Location services (optional)
    implementation(libs.play.services.location)

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation("io.coil-kt:coil-compose:2.5.0")


    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)


}

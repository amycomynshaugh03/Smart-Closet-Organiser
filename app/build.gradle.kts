// This build.gradle file defines the setup and dependencies for the Android project.
// The file includes:
// 1. Plugin configurations for Android application and Kotlin.
// 2. Android-specific settings, such as the SDK versions and build types.
// 3. Dependencies for core Android libraries, testing, logging, networking, and image loading.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("org.jetbrains.kotlin.plugin.parcelize")

    id("org.jetbrains.dokka") version "1.8.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"

    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android.ksp)
    alias(libs.plugins.compose.compiler)



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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    // Kotlin compiler options (use JVM target version 11)
    kotlinOptions {
        jvmTarget = "11"
    }


    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
            force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
    }


}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.inappmessaging)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)
    implementation(libs.timberkt)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.kotlinx.coroutines.android.v143)

    implementation(libs.androidx.lifecycle.viewmodel.ktx.v241)
    implementation(libs.androidx.lifecycle.livedata.ktx.v241)

    implementation(libs.picasso)
    implementation(libs.gson)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.play.services.location)

    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.github.erenalpaslan:removebg:1.0.4")

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


}

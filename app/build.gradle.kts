
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
    namespace = "ie.setu.project"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "ie.setu.project"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val geminiKey = project.findProperty("GEMINI_API_KEY")?.toString() ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")

        val removeBgKey = project.findProperty("REMOVE_BG_API_KEY")?.toString() ?: ""
        buildConfigField("String", "REMOVE_BG_API_KEY", "\"$removeBgKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation)
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


    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")


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
    implementation("androidx.compose.material:material-icons-extended") {
        exclude(group = "androidx.compose.material", module = "material")
    }

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.firebase.storage.ktx)

    implementation(libs.generativeai)

    implementation(libs.androidx.exifinterface)

    implementation(libs.googleid)

    implementation(libs.androidx.datastore.preferences)
}

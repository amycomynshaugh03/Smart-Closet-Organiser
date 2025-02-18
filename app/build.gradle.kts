

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ie.setu.project"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "ie.setu.project"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "WEATHER_API_KEY", "\"e1c8f42d6eb241bbbbe194422251802\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.slf4j.simple)
    implementation (libs.kotlin.logging)
    implementation (libs.timberkt)
    // Retrofit and Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp logging interceptor for debugging
    implementation(libs.logging.interceptor)

    // Lifecycle and Coroutine support
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v231)
    implementation(libs.androidx.lifecycle.livedata.ktx.v231)
    implementation(libs.kotlinx.coroutines.android.v150)




}
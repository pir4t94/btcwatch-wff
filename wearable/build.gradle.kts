plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.roklab.btcwatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.roklab.btcwatch"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Android Framework
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)

    // Wear OS - WFF (Wear Face Format) API
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wear.watchface)
    implementation(libs.androidx.wear.watchface.complications)

    // Google Play Services for Wearable
    implementation(libs.google.play.services.wearable)

    // Coroutines for async operations
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Kotlin
    implementation(libs.kotlin.stdlib)
}

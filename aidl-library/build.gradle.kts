plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

// Versión de la librería AIDL
val libraryVersion = "0.1.0"

android {
    namespace = "com.tcontur.aidl"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        // Agregar versión a BuildConfig
        buildConfigField("String", "LIBRARY_VERSION", "\"$libraryVersion\"")
        buildConfigField("long", "VERSION_CODE", "1")

        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

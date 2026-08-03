

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android) // Required for Kotlin support
    alias(libs.plugins.kotlin.kapt)    // <--- ADD THIS LINE
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)

}

android {
    namespace = "com.ackileo.telematics.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    ksp {
        arg("room.incremental", "true")
        // This tells KSP not to crash if it hits a type it doesn't recognize yet
        arg("ksp.incremental.interprocedural", "true")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Hilt
    api(project(":app:Domain"))

    // Hilt Navigation Compose
    implementation(libs.androidx.hilt.navigation.compose)
//ksp

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Room
    implementation(libs.dagger.hilt)
    kapt(libs.dagger.hilt.compiler)


    // ... rest of your dependencies
}
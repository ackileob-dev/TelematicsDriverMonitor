

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)

}

android {
    namespace = "com.ackileo.telematics"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ackileo.telematics"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    ksp {
        arg("room.incremental", "true")
        arg("plugin:com.google.devtools.ksp:incremental", "true")
        // ADD THIS LINE:
        arg("ksp.allow.missing.type", "true")
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
        compose = true
    }

    packaging {
        resources {
            pickFirsts += "META-INF/io.netty.versions.properties"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
            // Recommended additional excludes to prevent similar future errors
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    // --- App Modules ---
    implementation(project(":app:Data"))
    implementation(project(":app:Domain"))

    // --- Dependency Injection (Hilt) ---
// Inside app/build.gradle.kts dependencies block
// REMOVE the hardcoded lines:
// implementation("com.google.dagger:hilt-android:2.52")
// ksp("com.google.dagger:hilt-compiler:2.52")

// USE the catalog references:
    implementation(libs.dagger.hilt)
    implementation(libs.firebase.appdistribution.gradle)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    // --- Local Database (Room) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")


    // --- Firebase ---
    implementation(platform(libs.firebase.bom))

    // Add the dependencies for Firebase products you want to use
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    // --- Firebase ---

    // Removed -ktx
    implementation("com.google.firebase:firebase-storage")   // Removed -ktx
         // Removed -ktx
    // --- Networking (Updated to use Catalog) ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // --- Maps & Location (Updated to use Catalog) ---
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.google.maps.compose)
    implementation(libs.google.maps.services)
    implementation(libs.google.maps.utils)
    implementation(libs.accompanist.permissions)


    // This remains the same for Compose support

    // --- Compose UI ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")

    // --- AndroidX Core & Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // --- Unit Testing ---
    testImplementation(libs.junit)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // --- Instrumented Testing ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // --- Debug Tools ---
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // KEEP THESE


}



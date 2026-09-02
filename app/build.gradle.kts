

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)

}

val devApiScheme = (findProperty("DEV_API_SCHEME") as String?) ?: "http"
val devApiHost = (findProperty("DEV_API_HOST") as String?) ?: "localhost:5000"
val devApiBasePath = (findProperty("DEV_API_BASE_PATH") as String?) ?: "/api/"

val prodApiScheme = (findProperty("PROD_API_SCHEME") as String?) ?: "https"
val prodApiHost = (findProperty("PROD_API_HOST") as String?) ?: "api.telematicsmonitor.com"
val prodApiBasePath = (findProperty("PROD_API_BASE_PATH") as String?) ?: "/api/"
val mapsApiKey = (findProperty("MAPS_API_KEY") as String?) ?: ""

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
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    ksp {
        arg("room.incremental", "true")
        arg("plugin:com.google.devtools.ksp:incremental", "true")
        // ADD THIS LINE:
        arg("ksp.allow.missing.type", "true")
    }

    
    buildTypes {
        debug {
            buildConfigField("String", "API_SCHEME", "\"$devApiScheme\"")
            buildConfigField("String", "API_HOST", "\"$devApiHost\"")
            buildConfigField("String", "API_BASE_PATH", "\"$devApiBasePath\"")
            manifestPlaceholders["USES_CLEARTEXT_TRAFFIC"] = "true"
        }

        release {
            buildConfigField("String", "API_SCHEME", "\"$prodApiScheme\"")
            buildConfigField("String", "API_HOST", "\"$prodApiHost\"")
            buildConfigField("String", "API_BASE_PATH", "\"$prodApiBasePath\"")
            manifestPlaceholders["USES_CLEARTEXT_TRAFFIC"] = "false"
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
        buildConfig = true
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

androidComponents {
    beforeVariants(selector().withBuildType("release")) { variantBuilder ->
        // Local JVM unit tests are verified on debug variant.
        variantBuilder.enableUnitTest = false
    }
}

dependencies {
    // --- App Modules ---
    implementation(project(":app:Data"))
    implementation(project(":app:Domain"))

    // --- Dependency Injection (Hilt) ---
    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    // --- Local Database (Room) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Security ---
    implementation(libs.androidx.security.crypto)


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
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation(libs.kotlinx.coroutines.test)
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


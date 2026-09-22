plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.spoonsage.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.spoonsage.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // TODO: Replace with your own free key from https://spoonacular.com/food-api
        buildConfigField("String", "SPOONACULAR_API_KEY", "\"YOUR_SPOONACULAR_API_KEY_HERE\"")
        // No API key needed for the AI Coach anymore - it calls Gemini through
        // your Firebase project (Firebase AI Logic). See SETUP_FIREBASE.md.
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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
    // Core / Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room (offline cache - recipes, meal plan, grocery list)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore (small preference values - diet, dislikes)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Retrofit (Spoonacular API)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // Lets us .await() Firebase Tasks (auth, Firestore) as suspend calls
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Firebase (Auth, Firestore) and Gemini via Firebase AI Logic (AI Coach)
    // Note: as of Firebase BoM 34.0.0 (July 2025), the -ktx artifacts were
    // removed - Kotlin extensions now live in the main modules below.
    implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-ai")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Unit testing (Learning Unit 2: detailed unit testing)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("com.google.truth:truth:1.4.2")
}

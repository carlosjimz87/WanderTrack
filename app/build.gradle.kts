import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

val secretsProperties = Properties().apply {
    val secretsFile = rootProject.file("secrets.keystore")
    if (secretsFile.exists()) {
        load(secretsFile.inputStream())
    }
}

android {
    namespace = "com.carlosjimz87.wandertrack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.carlosjimz87.wandertrack"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GOOGLE_CLIENT_ID",
            "\"${secretsProperties["GOOGLE_CLIENT_ID"]}\""
        )

        buildConfigField(
            "String",
            "GOOGLE_MAPS_KEY",
            "\"${secretsProperties["GOOGLE_MAPS_KEY"]}\""
        )
        manifestPlaceholders["GOOGLE_MAPS_KEY"] = secretsProperties["GOOGLE_MAPS_KEY"] as Any
    }

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            buildConfigField("String", "FIREBASE_ENV", "\"dev\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "FIREBASE_ENV", "\"prod\"")
        }
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)

    // Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase modules
    implementation(libs.firebase.auth.ktx)
    implementation(libs.google.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.inappmessaging.ktx)
    implementation(libs.firebase.ml.modeldownloader.ktx)
    implementation(libs.firebase.crashlytics.ktx)

    // Google Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Google Maps Utils
    implementation(libs.android.maps.utils)

    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.play.services.tasks)
    testImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))
}
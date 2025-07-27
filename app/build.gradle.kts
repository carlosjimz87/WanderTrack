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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.carlosjimz87.wandertrack"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "2.0"

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
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"10.0.2.2\"")
            buildConfigField("int", "AUTH_EMULATOR_PORT", "9099")
            buildConfigField("int", "STORE_EMULATOR_PORT", "8080")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "FIREBASE_ENV", "\"prod\"")
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"unused\"")
            buildConfigField("int", "AUTH_EMULATOR_PORT", "0")
            buildConfigField("int", "STORE_EMULATOR_PORT", "0")
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

    // Material Icons Extended
    implementation(libs.androidx.material.icons.extended)

    // Google Fonts
    implementation(libs.androidx.ui.text.google.fonts)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Lottie & Splashscreen
    implementation(libs.lottie.compose)
    implementation(libs.androidx.core.splashscreen)

    // Nav3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Gson
    implementation(libs.gson)

    // Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase modules
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.inappmessaging.ktx)
    implementation(libs.firebase.ml.modeldownloader.ktx)
    implementation(libs.firebase.crashlytics.ktx)

    // Google Services
    implementation(libs.maps.compose)
    implementation(libs.google.maps)
    implementation(libs.google.auth)
    implementation(libs.android.maps.utils)
    implementation(libs.androidx.credentials)
    implementation(libs.googleid)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

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
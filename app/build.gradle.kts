import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "com.vinn.vhike"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vinn.vhike"
        minSdk = 26 // Lowered minSdk from 36 (too high) to 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // ADDED for Compose
        vectorDrawables {
            useSupportLibrary = true
        }
        buildFeatures {
            buildConfig = true
        }

        buildConfigField("String", "GITHUB_TOKEN", "\"${localProperties.getProperty("github.token")}\"")
        buildConfigField("String", "GITHUB_OWNER", "\"${localProperties.getProperty("github.owner")}\"")
        buildConfigField("String", "GITHUB_REPO", "\"${localProperties.getProperty("github.repo")}\"")
        buildConfigField("String", "GITHUB_TARGET_FOLDER", "\"${localProperties.getProperty("github.folder")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8" // <-- FIXED: Was "11"
    }
    buildFeatures {
        compose = true
    }
    // 2. REMOVE THIS ENTIRE BLOCK
    // composeOptions {
    //     kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    // }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    // REMOVED - We use Compose, not AppCompat
    // implementation(libs.androidx.appcompat)
     implementation(libs.material)
    // implementation(libs.androidx.activity)
    // implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui) // <-- FIXED: Was libs.androidx-compose-ui
    implementation(libs.androidx.compose.ui.graphics) // <-- FIXED: Was libs.androidx-compose-ui-graphics
    implementation(libs.androidx.compose.ui.tooling.preview) // <-- FIXED: Was libs.androidx-compose-ui-tooling.preview
    implementation(libs.androidx.compose.material3) // <-- FIXED: Was libs.androidx-compose-material3
    debugImplementation(libs.androidx.compose.ui.tooling) // <-- FIXED: Was libs.androidx-compose-ui.tooling

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    implementation("com.google.android.libraries.places:places:3.5.0")
    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room (Database)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Coil (Images)
    implementation(libs.coil.compose)

    implementation("org.kohsuke:github-api:1.321")

    implementation("com.google.maps.android:maps-compose:4.3.3")

    implementation("com.google.accompanist:accompanist-permissions:0.31.5-beta")

    implementation("com.google.android.gms:play-services-location:21.2.0")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

val openrouterApiKey: String = try {
    val props = Properties()
    val localPropsFile = project.rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        FileInputStream(localPropsFile).use { props.load(it) }
        props.getProperty("OPENROUTER_API_KEY", "")
    } else ""
} catch (_: Exception) { "" }

android {
    namespace = "com.r1n1os.jetpackcomposetemplateopensource"
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "com.r1n1os.jetpackcomposetemplateopensource"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openrouterApiKey\"")
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        disable += "UnsafeOptInUsageError"
    }
}

dependencies {
    /**
     * Basic
     * */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.expressive)
    /**
     * Compose
     * */
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    /**
     * Navigation
     * */
    implementation(libs.fragment.navigation.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    /**
     * Hilt
     * */
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.agp)
    implementation(libs.hilt.navigation.compose)
    /**
     * Coroutines
     * */
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    /**
     * Retrofit
     * */
    implementation(libs.retrofit)
    implementation(libs.gson)
    /**
     * Room Local Database
     * */
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    /**
     * CameraX
     * */
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    /**
     * ML Kit Barcode Scanning
     * */
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    /**
     * Gemini AI — REST API directa con OkHttp
     * */
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    /**
     * Test
     * */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.compose.material:material-icons-extended:1.7.8")
}
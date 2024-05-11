import java.util.Properties

plugins {
    id("com.android.application")
}

val signingProps = Properties()
val signingPropsFile = file("signing.properties")

if (signingPropsFile.exists()) {
    signingProps.load(signingPropsFile.inputStream())
} else {
    println("signing.properties not found, skipping release build configuration.")
}

android {
    namespace = "com.example.nfcaimereader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nfcaimereader"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionCode = System.getenv("CODE")?.toInt() ?: 1
        versionName = System.getenv("VERSION") ?: "1.0"
    }

    signingConfigs {
        if (signingPropsFile.exists()) {
            create("release") {
                storeFile = file(signingProps["KEYSTORE_FILE"] as String)
                storePassword = signingProps["KEYSTORE_PASSWORD"] as String
                keyAlias = signingProps["KEY_ALIAS"] as String
                keyPassword = signingProps["KEY_PASSWORD"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (signingPropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
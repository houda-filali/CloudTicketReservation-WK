plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cloudticketreservationwk"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cloudticketreservationwk"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // JUnit 4 only (remove JUnit 5 dependencies)
    testImplementation("junit:junit:4.13.2")

    // Android Test dependencies
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Remove the tasks.withType<Test> block for JUnit 4
plugins {
    alias(libs.plugins.android.application)
}
if (!System.getenv("CI").toBoolean()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.example.cloudticketreservationwk"
    compileSdk = 36 // changed from 34 to 36

    defaultConfig {
        applicationId = "com.example.cloudticketreservationwk"
        minSdk = 24
        targetSdk = 36 // changed from 34 to 36
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
    //testImplementation(libs.junit)
    // We're using junit 5, its in the requirements
    //testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

    // add this for junit
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")


    // Android Test dependencies
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core:1.5.0")

}

// DON'T REMOVE (for junit5)
tasks.withType<Test> {
    useJUnitPlatform()
}
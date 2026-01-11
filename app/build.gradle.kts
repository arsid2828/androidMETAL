plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.prova1"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.prova1"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.1"

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
    buildFeatures {
        viewBinding = true
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Google Maps Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Retrofit, Gson & Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Librerie per parsing XML (ATOM feeds)
    implementation("com.squareup.retrofit2:converter-simplexml:2.9.0")
    implementation("org.simpleframework:simple-xml:2.7.1") // <-- LIBRERIA MANCANTE

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.1")
}

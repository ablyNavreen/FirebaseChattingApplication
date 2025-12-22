plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.firebasechattingapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.firebasechattingapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.firebasechattingapplication"
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
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
//    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //coroutines
    implementation(libs.kotlinx.coroutines.android)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    //navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // sdp
    implementation(libs.sdp.android)

    // ssp
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("com.google.firebase:firebase-messaging-ktx")
    // Core Firebase SDK (Recommended)
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))

    implementation("com.google.firebase:firebase-storage")

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // AppAuth Library for secure OAuth flow
    implementation("net.openid:appauth:0.11.1")

    //zoom iamge
    implementation("com.jsibbold:zoomage:1.3.1")

    //glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

}
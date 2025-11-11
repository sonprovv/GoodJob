plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id ("kotlin-parcelize")
}

android {
    namespace = "com.project.job"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.project.job"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    androidResources {
        generateLocaleConfig = false
    }

    buildFeatures {
        viewBinding = true
    }

    // ✅ Tạo APK riêng theo CPU architecture để giảm kích thước
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a") // chỉ giữ kiến trúc phổ biến
            isUniversalApk = false               // không build universal APK
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit and Serialization
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

// Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

// OkHttp for Glide
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")

// CircleIndicator
    implementation("me.relex:circleindicator:2.1.6")
    
    // Circle ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Room
    implementation("androidx.room:room-ktx:2.7.1")
    implementation("androidx.room:room-runtime:2.7.1")
    kapt("androidx.room:room-compiler:2.7.1")

// Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

// Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")
    implementation("com.google.firebase:firebase-messaging-ktx")

// Lottie Animation
    implementation("com.airbnb.android:lottie:6.4.0")

// Google Sign In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

// Credentials
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")


    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // google map
    implementation("com.mapbox.maps:android:10.16.0")
    implementation("com.mapbox.navigationcore:android:3.12.0-beta.1")

    // json
    implementation("org.json:json:20230227")


}
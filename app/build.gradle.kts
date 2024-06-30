plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.doan"
    compileSdk = 34
    ndkVersion = "25.1.8937393"
    defaultConfig {

        externalNativeBuild {
            cmake {
            arguments("-DANDROID_STL=c++_shared")
            }
        }
        applicationId = "com.example.doan"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        exclude ("META-INF/NOTICE.md")
        exclude ("META-INF/LICENSE.md")
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
        jvmTarget = "1.8"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        prefab = true
    }
}

dependencies {
    implementation("com.google.firebase:firebase-database:21.0.0")
    val nav_version = "2.7.7"
    val lifecycle_version = "2.8.0"
    val coroutines = "1.3.9"
    val room_version = "2.6.1"
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.annotation:annotation:1.6.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Google Sign In SDK
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase UI
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation("com.firebaseui:firebase-ui-database:8.0.2")

    // Navigation component
    implementation ("androidx.navigation:navigation-fragment-ktx:${nav_version}")
    implementation ("androidx.navigation:navigation-ui-ktx:${nav_version}")
    //ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycle_version}")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")


    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    // OpenSSL
    implementation ("com.android.ndk.thirdparty:openssl:1.1.1l-beta-1")
    implementation ("com.android.ndk.thirdparty:curl:7.79.1-beta-1")
    // loading image lib
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // send email lib
    implementation ("com.sun.mail:android-mail:1.6.6")
    implementation ("com.sun.mail:android-activation:1.6.7")
}
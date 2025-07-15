plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mapdemo"
    compileSdk = 34

    defaultConfig {



        applicationId = "com.example.mapdemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // 设置支持的 SO 库架构（开发者可以根据需要，选择一个或多个平台的 so）
            abiFilters.addAll(listOf("armeabi", "armeabi-v7a", "arm64-v8a", "x86","x86_64"))
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

    buildFeatures {
        viewBinding = true

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("libs\\BaiduLBS_Android.jar"))
    implementation(libs.room.common)
    implementation(libs.room.common.jvm)
    implementation(files("libs\\Msc.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    val room_version = "2.6.1"
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("androidx.room:room-runtime:$room_version")

    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("com.github.bumptech.glide:glide:4.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("androidx.exifinterface:exifinterface:1.3.3")
    implementation("com.squareup.retrofit2:retrofit:2.0.2")
    implementation("com.squareup.retrofit2:converter-gson:2.0.2")
    implementation ("com.android.support:appcompat-v7")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")

}
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0-rc01")
    }
}

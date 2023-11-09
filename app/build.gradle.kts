import org.apache.tools.ant.util.JavaEnvUtils.VERSION_1_8

plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.lab6"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lab6"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }


}


dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.gms:play-services-maps:18.0.0")
    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.android.volley:volley:1.2.0")




    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

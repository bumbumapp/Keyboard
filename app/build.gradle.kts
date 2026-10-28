/*
 * Copyright (C) 2022 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Suppress needed until https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
@file:Suppress("DSL_SCOPE_VIOLATION")

import java.io.File

plugins {
    alias(libs.plugins.agp.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mannodermaus.android.junit5)
    alias(libs.plugins.mikepenz.aboutlibraries)
    id("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

android {
    namespace = "dev.patrickgold.florisboard"
    compileSdk = 36
    // NDK r28 links shared libraries with 16 KB page alignment by default.
    ndkVersion = "28.2.13676358"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.bumbumapps.keyboardpro"
        minSdk = 24
        targetSdk = 36
        // Play: versionCode 12 is live.
        versionCode = 13
        versionName = "1.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BUILD_COMMIT_HASH", "\"${getGitCommitHash()}\"")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }

        externalNativeBuild {
            cmake {
                cFlags("-fvisibility=hidden", "-DU_STATIC_IMPLEMENTATION=1")
                cppFlags("-fvisibility=hidden", "-std=c++17", "-fexceptions", "-ffunction-sections", "-fdata-sections", "-DU_DISABLE_RENAMING=1", "-DU_STATIC_IMPLEMENTATION=1")
                arguments("-DANDROID_STL=c++_static")
            }
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        sourceSets {
            maybeCreate("main").apply {
                assets {
                    srcDirs("src/main/assets", "src/main/icu4c/prebuilt/assets")
                }
                jniLibs {
                    srcDirs("src/main/icu4c/prebuilt/jniLibs")
                }
                java {
                    srcDirs("src/main/kotlin")
                }
            }
        }
    }

    bundle {
        language {
            // We disable language split because FlorisBoard does not use
            // runtime Google Play Service APIs and thus cannot dynamically
            // request to download the language resources for a specific locale.
            enableSplit = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    lint {
        abortOnError = false
        // Lint's own UElementAsPsi detector crashes on a Kotlin 2 lambda in this code base (lint bug).
        disable += "UElementAsPsi"
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isJniDebuggable = false

            ndk {
                // For running FlorisBoard on the emulator
                abiFilters += listOf("x86", "x86_64")
            }

            resValue("mipmap", "floris_app_icon", "@mipmap/logo")
            resValue("mipmap", "floris_app_icon_round", "@mipmap/logo_round")
            resValue("drawable", "floris_app_icon_foreground", "@drawable/keyboard")
            resValue("string", "floris_app_name", "Keyboard pro")
            // Google test ads in debug builds.
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            resValue("string", "admob_interstitial_id", "ca-app-pub-3940256099942544/1033173712")
        }


        release {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isMinifyEnabled = true
            isShrinkResources = true

            resValue("mipmap", "floris_app_icon", "@mipmap/logo")
            resValue("mipmap", "floris_app_icon_round", "@mipmap/logo_round")
            resValue("drawable", "floris_app_icon_foreground", "@drawable/keyboard")
            resValue("string", "floris_app_name", "Keyboard pro")
            manifestPlaceholders["admobAppId"] = "ca-app-pub-8444865753152507~4748689752"
            resValue("string", "admob_interstitial_id", "ca-app-pub-8444865753152507/1478145698")
        }


    }

    aboutLibraries {
        configPath = "app/src/main/config"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-Xjvm-default=all-compatibility",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.autofill)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.views)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.cache4k)
    implementation(libs.jetpref.datastore.model)
    implementation(libs.jetpref.datastore.ui)
    implementation(libs.jetpref.material.ui)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.mikepenz.aboutlibraries.core)
    implementation(libs.mikepenz.aboutlibraries.compose)

    testImplementation(libs.equalsverifier)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.roboelectric)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.runner.junit5)

    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

fun getGitCommitHash(short: Boolean = false): String {
    if (!File(".git").exists()) {
        return "null"
    }

    return try {
        val args = if (short) listOf("git", "rev-parse", "--short", "HEAD") else listOf("git", "rev-parse", "HEAD")
        val p = ProcessBuilder(args).redirectErrorStream(true).start()
        p.inputStream.bufferedReader().readText().trim().also { p.waitFor() }
    } catch (e: Exception) {
        "null"
    }
}

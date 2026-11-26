plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.jetbrains.kotlin.serialization)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.devlotfi.cipherbox.gateway"
  compileSdk {
    version = release(37) {
      minorApiLevel = 2
    }
  }

  defaultConfig {
    applicationId = "com.github.devlotfi.cipherbox.gateway"
    minSdk = 33
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    release {
      optimization {
        enable = true
        packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
      }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  packaging {
    resources {
      excludes += listOf("META-INF/INDEX.LIST", "META-INF/io.netty.versions.properties")
    }
  }

  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.androidx.material3.adaptive.navigation3)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.biometric)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)

  implementation(libs.tor.android)
  implementation(libs.hivemq.mqtt.client)
  implementation(platform(libs.hivemq.mqtt.client.proxy))

  ksp(libs.androidx.room3.compiler)
  implementation(libs.androidx.room3.runtime)
  implementation(libs.sqlcipher.android)
  implementation(libs.androidx.sqlite)

  implementation(ktorLibs.server.cio)
  implementation(ktorLibs.server.core)
  implementation(ktorLibs.server.cors)
  implementation(ktorLibs.server.contentNegotiation)
  implementation(ktorLibs.serialization.kotlinx.json)
  implementation(ktorLibs.server.routingOpenapi)
  implementation(ktorLibs.server.swagger)

  implementation(ktorLibs.client.okhttp)
  implementation(ktorLibs.client.core)
  implementation(ktorLibs.client.contentNegotiation)

  implementation(libs.compose.query)
  implementation(libs.compose.vstore)
  implementation(libs.compose.form)
  implementation(libs.kanyon)

  implementation(project(":shared"))
  implementation(project(":openapi"))
}
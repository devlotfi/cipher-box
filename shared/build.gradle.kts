plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.jetbrains.kotlin.serialization)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.github.devlotfi.cipherbox.shared"
  compileSdk {
    version = release(37)
  }

  defaultConfig {
    minSdk = 33
    externalNativeBuild {
      cmake {
        cppFlags += ""
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
  buildFeatures {
    compose = true
  }
  externalNativeBuild {
    cmake {
      path = file("src/main/cpp/CMakeLists.txt")
      version = "3.22.1"
    }
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
  implementation(libs.androidx.biometric)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.graphics.shapes)

  ksp(libs.androidx.room3.compiler)
  implementation(libs.androidx.room3.runtime)
  implementation(libs.sqlcipher.android)
  implementation(libs.androidx.sqlite)

  implementation(libs.bcprov.jdk18on)

  implementation(libs.compose.query)
  implementation(libs.compose.vstore)
  implementation(libs.compose.form)
  implementation(libs.kanyon)
}
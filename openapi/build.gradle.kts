plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.jetbrains.kotlin.serialization)
  alias(libs.plugins.openapi.generator)
}

openApiGenerate {
  generatorName.set("kotlin")
  library.set("jvm-ktor")
  remoteInputSpec.set(
    providers.gradleProperty("openApiUrl")
      .orElse("http://localhost:8080/openapi/documentation.yaml")
  )

  outputDir.set("$rootDir/openapi")
  packageName.set("com.github.devlotfi.cipherbox.openapi")
  skipValidateSpec.set(true)

  generateApiTests.set(false)
  generateModelTests.set(false)
  generateApiDocumentation.set(false)
  generateModelDocumentation.set(false)

  configOptions.set(
    mapOf(
      "useCoroutines" to "true",
      "serializationLibrary" to "kotlinx_serialization",
      "sourceFolder" to "src/main/java"
    )
  )
}

android {
  namespace = "com.github.devlotfi.cipherbox.openapi"
  compileSdk {
    version = release(37) {
      minorApiLevel = 2
    }
  }

  defaultConfig {
    minSdk = 33

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.material)

  implementation(libs.kotlin.stdlib.jdk8)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(ktorLibs.client.okhttp)
  implementation(ktorLibs.client.core)
  implementation(ktorLibs.client.cio)
  implementation(ktorLibs.client.contentNegotiation)
}
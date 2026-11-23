package com.github.devlotfi.cipherbox.client

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.compose.AppTheme

class MainActivity : ComponentActivity() {
  companion object {
    init {
      System.loadLibrary("shared")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    setContent {
      AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Main(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}
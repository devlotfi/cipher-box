package com.github.devlotfi.cipherbox.shared.composables


import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose.AppTheme

@Composable
fun LoadingView(
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    PolygonLoadingIndicator()
  }
}

@Preview(
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
fun LoadingViewPreview() {
  AppTheme {
    Scaffold { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        LoadingView(modifier = Modifier.fillMaxSize())
      }
    }
  }
}
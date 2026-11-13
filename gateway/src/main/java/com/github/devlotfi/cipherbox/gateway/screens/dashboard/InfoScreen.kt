package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InfoScreen(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.background(MaterialTheme.colorScheme.error)
  ) {
    Text("Info screen")
  }
}
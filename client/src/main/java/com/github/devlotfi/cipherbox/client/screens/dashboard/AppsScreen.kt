package com.github.devlotfi.cipherbox.client.screens.dashboard

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.devlotfi.cipherbox.client.LocalNavigation
import com.github.devlotfi.cipherbox.client.ScreenNavigationKeys

@Composable
fun AppsScreen() {
  val localNavigation = LocalNavigation.current

  Text("Apps")
  Button(onClick = {
    localNavigation.backStack.add(ScreenNavigationKeys.MessagesApp)
  }) {
    Text("test")
  }
}
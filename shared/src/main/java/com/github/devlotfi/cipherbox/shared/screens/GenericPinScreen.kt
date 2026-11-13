package com.github.devlotfi.cipherbox.shared.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.devlotfi.cipherbox.shared.composables.Numpad
import com.github.devlotfi.cipherbox.shared.composables.SimpleTopBar
import com.github.devlotfi.composevstore.DataStore

@Composable
fun GenericPinScreen(
  modifier: Modifier = Modifier,
  dataStore: DataStore,
  @DrawableRes logo: Int,
  appName: String,
  title: String,
  titleIcon: @Composable () -> Unit = {},
  subTitle: String? = null,
  error: String? = null,
  clearError: () -> Unit,
  loading: Boolean = false,
  onPin: (pin: String) -> Unit = {}
) {
  val scrollState = rememberScrollState()
  var pin by dataStore.rememberStateInDataStore("PIN") { "" }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopBar(
        appName = appName,
        logo = logo
      )
    },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
        .verticalScroll(scrollState),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 20.dp, horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          titleIcon()
          Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }

        if (subTitle != null) Text(
          subTitle,
          fontSize = 18.sp,
          fontWeight = FontWeight.Normal,
          textAlign = TextAlign.Center
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Numpad(
          modifier = Modifier.width(300.dp),
          dataStore = dataStore,
          current = pin,
          loading = loading,
          error = error,
          onNumberClick = { number ->
            val localLen = pin.length
            var localPin = pin
            if (localLen < 10) {
              pin += number.toString()
              localPin += number.toString()
              if (localLen == 9) {
                onPin(localPin)
              }
            }
          },
          onBackspaceClick = {
            clearError()
            pin = pin.dropLast(1)
          },
        )
      }
    }
  }
}
package com.github.devlotfi.cipherbox.gateway.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.devlotfi.cipherbox.gateway.NavigationKeys
import com.github.devlotfi.cipherbox.gateway.providers.LocalNavigation
import com.github.devlotfi.cipherbox.shared.R as SharedR

@Composable
fun WelcomeScreen(
  modifier: Modifier = Modifier
) {
  val localNavigation = LocalNavigation.current
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.radialGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
              Color.Transparent
            ),
            radius = 700f
          )
        )
    ) {
      Column(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Centered content
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(scrollState),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            painter = painterResource(SharedR.drawable.cipher_box_gateway_logo),
            contentDescription = null,
            modifier = Modifier.size(150.dp),
            tint = MaterialTheme.colorScheme.onBackground
          )

          Spacer(modifier = Modifier.height(50.dp))

          Text(
            "Welcome to cipher box gateway",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 35.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            "Initial setup is required to start using the app",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            localNavigation.backStack.clear()
            localNavigation.backStack.add(NavigationKeys.SetPin)
          },
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Start", modifier = Modifier.padding(5.dp))
        }
      }
    }
  }
}
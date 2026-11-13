package com.github.devlotfi.cipherbox.shared.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.devlotfi.cipherbox.shared.global.FontFamilies

@Composable
fun SimpleTopBar(
  appName: String,
  @DrawableRes logo: Int
) {
  TopAppBar(
    modifier = Modifier.clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
      titleContentColor = MaterialTheme.colorScheme.onBackground,
    ),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          painter = painterResource(logo),
          contentDescription = "logo",
          modifier = Modifier.size(40.dp),
          tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(15.dp))
        Text(
          appName,
          fontSize = 25.sp,
          fontWeight = FontWeight.Light,
          fontFamily = FontFamilies.Silkscreen
        )
      }
    }
  )
}
package com.github.devlotfi.cipherbox.shared.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionTitle(
  modifier: Modifier = Modifier,
  text: String = "Settings",
  imageVector: ImageVector,
  contentDescription: String? = null,

  // Box
  boxModifier: Modifier = Modifier
    .clip(RoundedCornerShape(10.dp))
    .background(MaterialTheme.colorScheme.primary)
    .padding(3.dp),

  // Icon
  iconModifier: Modifier = Modifier.size(30.dp),
  iconTint: Color = MaterialTheme.colorScheme.onPrimary,

  // Text
  textModifier: Modifier = Modifier,
  fontSize: TextUnit = 18.sp,
  fontWeight: FontWeight = FontWeight.Bold,
) {
  Row(
    modifier = modifier.padding(bottom = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = boxModifier) {
      Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = iconModifier,
        tint = iconTint
      )
    }

    Text(
      text = text,
      modifier = textModifier,
      fontSize = fontSize,
      fontWeight = fontWeight
    )
  }
}
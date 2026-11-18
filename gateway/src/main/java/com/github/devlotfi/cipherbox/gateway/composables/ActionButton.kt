package com.github.devlotfi.cipherbox.gateway.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButton(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  title: String,
  onClick: () -> Unit,
  enabled: Boolean = true,
) {
  OutlinedCard(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    onClick = onClick,
    enabled = enabled,
  ) {
    Column(
      modifier = Modifier
        .padding(vertical = 15.dp)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(5.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(30.dp)
      )
      Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
  }
}
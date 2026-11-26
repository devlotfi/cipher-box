package com.github.devlotfi.cipherbox.client.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.devlotfi.cipherbox.client.R

enum class CardStatus(
  val icon: ImageVector,
  @StringRes val text: Int,
) {
  ACTIVE(
    icon = com.github.devlotfi.cipherbox.shared.icons.outlined.play_arrow,
    text = R.string.active
  ),
  RESTARTING(
    icon = com.github.devlotfi.cipherbox.shared.icons.outlined.refresh,
    text = R.string.restarting
  ),
  INACTIVE(
    icon = com.github.devlotfi.cipherbox.shared.icons.outlined.stop,
    text = R.string.inactive
  )
}

@Composable
fun StatusCard(
  modifier: Modifier = Modifier,
  title: String,
  status: CardStatus,
  icon: ImageVector,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = when (status) {
        CardStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        CardStatus.RESTARTING -> MaterialTheme.colorScheme.error
        CardStatus.INACTIVE -> MaterialTheme.colorScheme.error
      }
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
    ) {
      Row(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(vertical = 5.dp, horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Icon(
          imageVector = status.icon,
          contentDescription = null,
          modifier = Modifier.size(28.dp)
        )
        Text(stringResource(status.text), fontSize = 12.sp, fontWeight = FontWeight.Medium)
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 10.dp, top = 35.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black)
      }
    }
  }

}
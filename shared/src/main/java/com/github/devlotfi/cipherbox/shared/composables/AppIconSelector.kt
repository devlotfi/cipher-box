package com.github.devlotfi.cipherbox.shared.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.cipherbox.shared.utils.AppIcon
import com.github.devlotfi.cipherbox.shared.utils.AppIconManager
import com.github.devlotfi.cipherbox.shared.utils.findActivity
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppIconButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  icon: AppIcon,
  @DrawableRes defaultIcon: Int,
  selected: Boolean = false,
) {
  OutlinedCard(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    onClick = onClick,
    colors = CardDefaults.cardColors(
      containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
      } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
      }
    )
  ) {
    Column(
      modifier = modifier
        .height(140.dp)
        .width(100.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(icon.icon ?: defaultIcon),
        contentDescription = null,
        modifier = Modifier.width(60.dp),
      )
      Spacer(modifier = Modifier.height(10.dp))

      Text(icon.label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
  }
}

@Composable
fun AppIconSelector(
  modifier: Modifier = Modifier,
  dataStore: DataStore,
  coroutineScope: CoroutineScope,
  @DrawableRes defaultIcon: Int,
  @StyleRes defaultSplashTheme: Int
) {
  val scrollState = rememberScrollState()
  val context = LocalContext.current
  val localSnackbar = LocalSnackbar.current
  var selected by dataStore.rememberStateInDataStore("APP_ICON") {
    AppIconManager.getCurrentIcon(
      context
    )
  }

  Column() {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp)
        .horizontalScroll(scrollState),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      AppIcon.entries.forEach { icon ->
        AppIconButton(
          defaultIcon = defaultIcon,
          icon = icon,
          selected = icon == selected,
          onClick = { selected = icon }
        )
      }
    }

    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 20.dp)
    ) {
      Button(
        shape = RoundedCornerShape(15.dp),
        enabled = selected != AppIconManager.getCurrentIcon(context),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          context.findActivity()?.let {
            AppIconManager.setIcon(it, selected, defaultSplashTheme)
            coroutineScope.launch {
              localSnackbar.snackbarHostState.showSnackbar(
                message = "Action Successful",
                duration = SnackbarDuration.Short
              )
            }
          }
        },
      ) {
        Text("Save", modifier = Modifier.padding(7.dp))
      }
    }
  }
}
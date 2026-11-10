package com.github.devlotfi.cipherbox.shared.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme
import com.github.devlotfi.cipherbox.shared.R

@Composable
fun EmptyView(
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = com.github.devlotfi.cipherbox.shared.icons.list,
        contentDescription = "empty",
        modifier = Modifier.size(90.dp),
      )
      Spacer(modifier = Modifier.height(5.dp))
      Text(
        stringResource(R.string.no_content),
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
      )
    }
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
fun EmptyViewPreview() {
  AppTheme {
    Scaffold { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        EmptyView(modifier = Modifier.fillMaxSize())
      }
    }
  }
}
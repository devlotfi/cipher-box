package com.github.devlotfi.cipherbox.shared.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun ErrorView(
  modifier: Modifier = Modifier,
  retry: () -> Unit = {}
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
        imageVector = com.github.devlotfi.cipherbox.shared.icons.error,
        contentDescription = "error",
        modifier = Modifier.size(90.dp),
        tint = MaterialTheme.colorScheme.error
      )
      Spacer(modifier = Modifier.height(5.dp))
      Text(
        stringResource(R.string.error_occured),
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = MaterialTheme.colorScheme.error
      )
      Spacer(modifier = Modifier.height(20.dp))
      Button(
        onClick = { retry() },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        )
      ) {
        Text(stringResource(R.string.retry))
      }
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
fun ErrorViewPreview() {
  AppTheme {
    Scaffold { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        ErrorView(modifier = Modifier.fillMaxSize())
      }
    }
  }
}
package com.github.devlotfi.cipherbox.shared.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.AppTheme
import com.github.devlotfi.cipherbox.shared.icons.outlined.backspace
import com.github.devlotfi.composevstore.DataStore

@Composable
fun Numpad(
  modifier: Modifier = Modifier,
  dataStore: DataStore,
  current: String,
  onNumberClick: (Int) -> Unit,
  onBackspaceClick: () -> Unit,
  loading: Boolean = false,
  error: String? = null
) {
  var randomDigits by remember { mutableStateOf((0..9).shuffled().toTypedArray()) }
  val buttons = listOf(
    randomDigits[0], randomDigits[1], randomDigits[2],
    randomDigits[3], randomDigits[4], randomDigits[5],
    randomDigits[6], randomDigits[7], randomDigits[8],
    null, randomDigits[9], -1
  )

  Box(
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.alpha(if (loading) 0.5f else 1.0f),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      if (error != null) Text(
        error, fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(15.dp))

      Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(100.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(7.dp),
          modifier = Modifier.padding(10.dp)
        ) {
          for (i in 1..10) {
            Box(
              modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .alpha(
                  if (i > current.length) 0.2f else 1f
                )
                .background(
                  if (error != null)
                    MaterialTheme.colorScheme.error
                  else
                    MaterialTheme.colorScheme.onBackground
                )

            ) {}
          }
        }
      }
      Spacer(modifier = Modifier.height(50.dp))

      Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        buttons.chunked(3).forEachIndexed { rowIndex, rowButtons ->
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            rowButtons.forEachIndexed { colIndex, number ->
              Box(
                modifier = Modifier.weight(1f)
              ) {
                when (number) {
                  null -> {
                    Spacer(
                      Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                    )
                  }

                  -1 -> {
                    NumpadButton(
                      enabled = !loading,
                      content = {
                        Icon(
                          imageVector = backspace,
                          contentDescription = "backspace",
                          modifier = Modifier.size(35.dp)
                        )
                      },
                      onClick = onBackspaceClick,
                      color = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                  }

                  else -> {
                    NumpadButton(
                      enabled = !loading,
                      content = {
                        Text(
                          number.toString(),
                          fontSize = 30.sp,
                          fontWeight = FontWeight.Medium
                        )
                      },
                      onClick = { onNumberClick(number) },
                      color = if (
                        rowIndex == 0 && colIndex == 1 ||
                        rowIndex == 1 && colIndex == 0 ||
                        rowIndex == 1 && colIndex == 2 ||
                        rowIndex == 2 && colIndex == 1
                      )
                        MaterialTheme.colorScheme.surfaceContainerLow
                      else null
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    if (loading) LoadingView()
  }
}

@Composable
private fun NumpadButton(
  modifier: Modifier = Modifier,
  color: Color? = null,
  content: @Composable () -> Unit,
  onClick: () -> Unit,
  enabled: Boolean = false
) {
  Surface(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(1f),
    shape = RoundedCornerShape(35.dp),
    color = color ?: MaterialTheme.colorScheme.surfaceContainerHigh,
    enabled = enabled,
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      content()
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
fun NumberPadPreview() {
  val dataStore = remember { DataStore() }

  AppTheme {
    Scaffold { innerPadding ->
      Column(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(0.8f)
        ) {
          Numpad(
            dataStore = dataStore,
            current = "123456",
            onNumberClick = { number ->
              println("Number: $number")
            },
            onBackspaceClick = {
              println("Backspace")
            },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}
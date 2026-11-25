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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.devlotfi.cipherbox.shared.global.KanyonFormDefinition
import com.github.devlotfi.cipherbox.shared.provider.LocalLoginDb
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.cipherbox.shared.utils.AppIcon
import com.github.devlotfi.cipherbox.shared.utils.CamouflageManager
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import com.github.devlotfi.cipherbox.shared.utils.findActivity
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberMutation
import com.github.devlotfi.composevstore.DataStore
import com.github.devlotfi.kanyon.schema.FieldSchema
import com.github.devlotfi.kanyon.validateHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
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

class CamouflagePinForm(camouflagePin: String) : KanyonFormDefinition() {
  init {
    validateOnChange = false
    validateOnBlur = true
  }

  val camouflagePin by textField(camouflagePin ?: "") {
    FieldSchema().string().length(10).validateHandler({
      it.all { it -> it.isDigit() }
    }, "Must be numeric").build()
  }
}

@Composable
fun CamouflageSelector(
  modifier: Modifier = Modifier,
  dataStore: DataStore,
  queryClient: QueryClient,
  coroutineScope: CoroutineScope,
  @DrawableRes defaultIcon: Int,
  @StyleRes defaultSplashTheme: Int
) {
  val scrollState = rememberScrollState()
  val context = LocalContext.current
  val localSnackbar = LocalSnackbar.current
  val localLoginDb = LocalLoginDb.current
  val loginDb = requireNotNull(localLoginDb.loginDbManager.db)
  var selected by dataStore.rememberStateInDataStore("APP_ICON") {
    CamouflageManager.getCurrentIcon(context)
  }

  val loginDataFlow = remember {
    loginDb.loginDataDao().observe().map { RoomFlowData(loading = false, data = it) }
  }
  val loginDataFlowData by loginDataFlow.collectAsStateWithLifecycle(
    initialValue = RoomFlowData(
      loading = true
    )
  )

  val form by dataStore.rememberValueInDataStore("CAMOUFLAGE_PIN_FORM") {
    CamouflagePinForm(loginDataFlowData.data?.camouflagePin ?: "")
  }

  val saveCamouflageMutation = queryClient.rememberMutation {
    mutateKey = listOf("SAVE_CAMOUFLAGE")
    mutateFn { params: Unit ->
      loginDataFlowData.data?.let {
        val loginDataEntity = it.copy(
          camouflagePin = form.camouflagePin.value
        )
        loginDb.loginDataDao().save(loginDataEntity)
      }
      context.findActivity()?.let {
        CamouflageManager.setIcon(it, selected, defaultSplashTheme)
      }
    }
    onSuccess {
      println("Success")
      coroutineScope.launch { localSnackbar.snackbarHostState.showSnackbar("Action success") }
    }
    onError {
      it.printStackTrace()
      coroutineScope.launch { localSnackbar.snackbarHostState.showSnackbar("An error occured") }
    }
  }

  LaunchedEffect(loginDataFlowData.data?.camouflagePin) {
    loginDataFlowData.data?.let {
      form.camouflagePin.value = loginDataFlowData.data!!.camouflagePin ?: ""
    }
  }

  if (loginDataFlowData.loading) {
    LoadingView()
    return
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
      ValidatedOutlinedSecureTextField(
        textFormFieldData = form.camouflagePin,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
          .fillMaxWidth(),
        label = { Text("Camouflage PIN") },
      )
      Spacer(modifier = Modifier.height(10.dp))

      Button(
        shape = RoundedCornerShape(15.dp),
        enabled = selected != CamouflageManager.getCurrentIcon(context) || form.camouflagePin.value != loginDataFlowData.data?.camouflagePin,
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          if (form.validate()) {
            saveCamouflageMutation.mutate(Unit)
          }
        },
      ) {
        Text("Save", modifier = Modifier.padding(7.dp))
      }
    }
  }
}
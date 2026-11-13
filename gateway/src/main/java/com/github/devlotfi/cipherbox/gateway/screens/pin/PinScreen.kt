package com.github.devlotfi.cipherbox.gateway.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.NavigationKeys
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.gateway.providers.LocalAppDatabase
import com.github.devlotfi.cipherbox.gateway.providers.LocalKeystore
import com.github.devlotfi.cipherbox.gateway.providers.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.shared.crypto.AESGCMUtils
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreAuthenticator
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreManager
import com.github.devlotfi.composequery.QueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberMutation
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.example.utils.ARGON2IDUtils
import kotlin.time.Duration.Companion.milliseconds
import com.github.devlotfi.cipherbox.shared.R as SharedR

class PinScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  val queryCache = QueryCache()
  val queryClient = QueryClient(viewModelScope, queryCache)
  var error by mutableStateOf<String?>(null)
}

data class VerifyPinMutationParams(
  val pin: String
)

data class InitSqlCipherDbMutationParams(
  val password: String
)

@Composable
fun PinScreen(
  modifier: Modifier = Modifier,
  viewModel: PinScreenViewModel = viewModel(),
) {
  val context = LocalContext.current
  val activity = context as FragmentActivity
  val localAppDatabase = LocalAppDatabase.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current
  val localKeyStore = LocalKeystore.current
  val localNavigation = LocalNavigation.current
  val authenticator = remember {
    KeyStoreAuthenticator(activity)
  }

  val initSqlCipherDbMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("INIT_SQLCIPHER")
    mutateFn { params: InitSqlCipherDbMutationParams ->
      localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.init(context, params.password)
    }
    onSuccess {
      println("SqlCipher init success")
      localNavigation.backStack.clear()
      localNavigation.backStack.add(NavigationKeys.Dashboard)
    }
    onError {
      it.printStackTrace()
      viewModel.error = "An error occured"
    }
  }

  val verifyPinMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("VERIFY_PIN")
    mutateFn { params: VerifyPinMutationParams ->
      localAppDatabase.appDatabaseManager.init(context)
      if (localAppDatabase.appDatabaseManager.db == null) throw Exception("Error")
      val appData =
        localAppDatabase.appDatabaseManager.db!!.appDataDao().get() ?: throw Exception("Error")

      if (params.pin == appData.loginData.duressPin) {
        try {
          localAppDatabase.appDatabaseManager.delete(context)
        } catch (e: Exception) {
        }
        try {
          localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.delete(context)
        } catch (e: Exception) {
        }
        try {
          localKeyStore.sqlCipherDatabaseKey.delete()
        } catch (e: Exception) {
        }
        delay(2000.milliseconds)
        activity.finish()
      }

      val pinAesKey = withContext(Dispatchers.Default) {
        val aesKey = ARGON2IDUtils.deriveKey(params.pin, appData.loginData.argon2IdData.salt)
        return@withContext aesKey
      }

      val encrypted = KeyStoreManager.AesGcmEncryptedData(
        ciphertext = appData.loginData.aesGcmData.ciphertext,
        iv = appData.loginData.aesGcmData.ketStoreIv
      )
      val cipher = localKeyStore.sqlCipherDatabaseKey.createDecryptionCipher(encrypted)
      authenticator.authenticate(
        cipher = cipher,
        onSuccess = { authenticatedCipher ->
          try {
            val decryptedKeystore = localKeyStore.sqlCipherDatabaseKey.decrypt(
              encrypted,
              authenticatedCipher
            )
            val sqlCipherPassword = AESGCMUtils.decrypt(
              ciphertext = decryptedKeystore,
              iv = appData.loginData.aesGcmData.pinIv,
              key = pinAesKey
            )
            initSqlCipherDbMutation.mutate(
              InitSqlCipherDbMutationParams(
                password = sqlCipherPassword
              )
            )
          } catch (e: Exception) {
            e.printStackTrace()
            viewModel.error = "An error occured"
          }
        },
        onError = { error ->
          println(error)
          viewModel.error = "An error occured"
        }
      )
    }
    onSuccess {
      println("Key verification success")
    }
    onError {
      it.printStackTrace()
      viewModel.error = "An error occure"
    }
  }

  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = SharedR.drawable.cipher_box_gateway_logo,
    appName = stringResource(R.string.app_name),
    title = "Enter PIN",
    titleIcon = {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          com.github.devlotfi.cipherbox.shared.icons.outlined.lock,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
          tint = MaterialTheme.colorScheme.onPrimary
        )
      }
    },
    error = viewModel.error,
    clearError = {
      viewModel.error = null
    },
    loading = verifyPinMutation.loading || initSqlCipherDbMutation.loading,
    onPin = {
      println(it)
      verifyPinMutation.mutate(
        VerifyPinMutationParams(
          pin = it
        )
      )
    }
  )
}
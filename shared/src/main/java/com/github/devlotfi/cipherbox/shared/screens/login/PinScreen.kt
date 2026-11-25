package com.github.devlotfi.cipherbox.shared.screens.login

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.devlotfi.cipherbox.shared.crypto.AESGCMUtils
import com.github.devlotfi.cipherbox.shared.global.KeyStoreKeyAliases
import com.github.devlotfi.cipherbox.shared.provider.LocalLoginDb
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
  backStack: NavBackStack<NavKey>,
  initSqlcipherDb: suspend (password: String) -> Unit,
  deleteSqlcipherDb: suspend () -> Unit,
  onSuccess: () -> Unit,
  appName: String,
  @DrawableRes logo: Int
) {
  val context = LocalContext.current
  val activity = context as FragmentActivity
  val localLoginDb = LocalLoginDb.current
  val authenticator = remember { KeyStoreAuthenticator(activity) }
  val sqlCipherDbKey = remember { KeyStoreManager(KeyStoreKeyAliases.SQLCIPHER_DB_KEY) }

  val initSqlCipherDbMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("INIT_SQLCIPHER")
    mutateFn { params: InitSqlCipherDbMutationParams ->
      initSqlcipherDb(params.password)
    }
    onSuccess {
      println("SqlCipher init success")
      backStack.clear()
      onSuccess()
    }
    onError {
      it.printStackTrace()
      viewModel.error = "An error occured"
    }
  }

  val verifyPinMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("VERIFY_PIN")
    mutateFn { params: VerifyPinMutationParams ->
      localLoginDb.loginDbManager.init(context)
      if (localLoginDb.loginDbManager.db == null) throw Exception("Error")
      val appData =
        localLoginDb.loginDbManager.db!!.loginDataDao().get() ?: throw Exception("Error")

      if (params.pin == appData.duressPin) {
        try {
          localLoginDb.loginDbManager.delete(context)
        } catch (e: Exception) {
        }
        try {
          deleteSqlcipherDb()
        } catch (e: Exception) {
        }
        try {
          sqlCipherDbKey.delete()
        } catch (e: Exception) {
        }
        delay(2000.milliseconds)
        activity.finish()
      }

      val pinAesKey = withContext(Dispatchers.Default) {
        val aesKey = ARGON2IDUtils.deriveKey(params.pin, appData.argon2IdData.salt)
        return@withContext aesKey
      }

      val encrypted = KeyStoreManager.AesGcmEncryptedData(
        ciphertext = appData.aesGcmData.ciphertext,
        iv = appData.aesGcmData.ketStoreIv
      )
      val cipher = sqlCipherDbKey.createDecryptionCipher(encrypted)
      authenticator.authenticate(
        cipher = cipher,
        onSuccess = { authenticatedCipher ->
          try {
            val decryptedKeystore = sqlCipherDbKey.decrypt(
              encrypted,
              authenticatedCipher
            )
            val sqlCipherPassword = AESGCMUtils.decrypt(
              ciphertext = decryptedKeystore,
              iv = appData.aesGcmData.pinIv,
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
    logo = logo,
    appName = appName,
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
package com.github.devlotfi.cipherbox.shared.composables

import android.content.ClipData
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.composeform.TextFormFieldData
import com.github.devlotfi.composeform.onBlur
import kotlinx.coroutines.launch

@Composable
fun <T> ValidatedOutlinedSecureTextField(
  modifier: Modifier = Modifier,
  textFormFieldData: TextFormFieldData<T>,
  enableCopyButton: Boolean = false,
  enableVisibilityToggle: Boolean = true,
  state: TextFieldState? = null,
  enabled: Boolean = true,
  textStyle: TextStyle = LocalTextStyle.current,
  labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Cutout(),
  label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  prefix: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  inputTransformation: InputTransformation? = null,
  textObfuscationMode: TextObfuscationMode = TextObfuscationMode.RevealLastTyped,
  textObfuscationCharacter: Char = '\u2022',
  keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
  onKeyboardAction: KeyboardActionHandler? = null,
  shape: Shape = OutlinedTextFieldDefaults.shape,
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
  interactionSource: MutableInteractionSource? = null,
) {
  val scope = rememberCoroutineScope()
  val clipboard = LocalClipboard.current
  val localSnackbar = LocalSnackbar.current

  val fieldState = state ?: textFormFieldData.state
  var passwordVisible by rememberSaveable { mutableStateOf(false) }

  OutlinedSecureTextField(
    state = fieldState,
    modifier = modifier.onBlur(textFormFieldData),
    enabled = enabled,
    textStyle = textStyle,
    labelPosition = labelPosition,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = {
      if (trailingIcon != null || enableCopyButton || enableVisibilityToggle) Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (trailingIcon != null) trailingIcon()

        if (enableVisibilityToggle) IconButton(
          onClick = { passwordVisible = !passwordVisible }
        ) {
          Icon(
            imageVector = if (passwordVisible)
              com.github.devlotfi.cipherbox.shared.icons.outlined.visibility_off
            else
              com.github.devlotfi.cipherbox.shared.icons.outlined.visibility,
            contentDescription = if (passwordVisible) "Hide" else "Show",
            modifier = Modifier.size(30.dp)
          )
        }

        if (enableCopyButton) IconButton(
          onClick = {
            scope.launch {
              clipboard.setClipEntry(
                ClipEntry(ClipData.newPlainText("Text", fieldState.text))
              )
              localSnackbar.snackbarHostState.showSnackbar("Copied")
            }
          }
        ) {
          Icon(
            imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.content_copy,
            contentDescription = "Copy",
            modifier = Modifier.size(30.dp)
          )
        }
      }
    },
    prefix = prefix,
    suffix = suffix,
    supportingText = {
      if (textFormFieldData.error != null) Text(textFormFieldData.error!!)
      else if (supportingText != null) supportingText()
    },
    isError = isError || textFormFieldData.error != null,
    inputTransformation = inputTransformation ?: textFormFieldData.inputTransformation,
    textObfuscationMode = if (passwordVisible) TextObfuscationMode.Visible else textObfuscationMode,
    textObfuscationCharacter = textObfuscationCharacter,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    shape = shape,
    colors = colors,
    interactionSource = interactionSource,
  )
}
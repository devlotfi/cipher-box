package com.github.devlotfi.cipherbox.shared.composables

import android.content.ClipData
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.composeform.TextFormFieldData
import com.github.devlotfi.composeform.onBlur
import kotlinx.coroutines.launch

@Composable
fun <T> ValidatedOutlinedTextField(
  modifier: Modifier = Modifier,
  textFormFieldData: TextFormFieldData<T>,
  enableCopyButton: Boolean = false,
  state: TextFieldState? = null,
  enabled: Boolean = true,
  readOnly: Boolean = false,
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
  outputTransformation: OutputTransformation? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  onKeyboardAction: KeyboardActionHandler? = null,
  lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
  onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
  scrollState: ScrollState = rememberScrollState(),
  shape: Shape = OutlinedTextFieldDefaults.shape,
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
  interactionSource: MutableInteractionSource? = null,
) {
  val scope = rememberCoroutineScope()
  val clipboard = LocalClipboard.current
  val localSnackbar = LocalSnackbar.current

  OutlinedTextField(
    state = state ?: textFormFieldData.state,
    modifier = modifier.onBlur(textFormFieldData),
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    labelPosition = labelPosition,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = {
      if (trailingIcon != null || enableCopyButton) Row(
        modifier = Modifier
          .padding(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (trailingIcon != null) trailingIcon()

        if (enableCopyButton) IconButton(
          onClick = {
            scope.launch {
              clipboard.setClipEntry(
                ClipEntry(
                  ClipData.newPlainText("Text", (state ?: textFormFieldData.state).text)
                )
              )
              localSnackbar.snackbarHostState.showSnackbar("Copied")
            }
          }) {
          Icon(
            imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.content_copy,
            contentDescription = null,
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
    outputTransformation = outputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    lineLimits = lineLimits,
    onTextLayout = onTextLayout,
    scrollState = scrollState,
    shape = shape,
    colors = colors,
    interactionSource = interactionSource,
  )
}
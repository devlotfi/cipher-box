package com.github.devlotfi.cipherbox.shared.composables

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import com.github.devlotfi.composeform.TextFormFieldData
import com.github.devlotfi.composeform.onBlur

@Suppress("UNCHECKED_CAST")
@Composable
fun <T> ValidatedTextField(
  modifier: Modifier = Modifier,
  textFormFieldData: TextFormFieldData<T>,
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
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    supportingText = {
      if (textFormFieldData.error != null) Text(textFormFieldData.error!!)
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
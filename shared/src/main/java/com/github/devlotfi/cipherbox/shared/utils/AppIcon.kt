package com.github.devlotfi.cipherbox.shared.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import com.github.devlotfi.cipherbox.shared.R

enum class AppIcon(
  val aliasName: String,
  val label: String,
  @DrawableRes val icon: Int? = null,
  @StyleRes val splashTheme: Int? = null
) {
  DEFAULT(
    aliasName = ".DefaultAlias",
    label = "Default",
  ),
  CALCULATOR(
    aliasName = ".CalculatorCamouflageAlias",
    label = "Calculator",
    icon = R.drawable.calculator_camouflage_logo,
    splashTheme = R.style.Theme_CipherBox_Splash_Calculator
  ),
}
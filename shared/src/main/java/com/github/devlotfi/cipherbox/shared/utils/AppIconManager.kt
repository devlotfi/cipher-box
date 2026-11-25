package com.github.devlotfi.cipherbox.shared.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StyleRes

object AppIconManager {
  fun getCurrentIcon(context: Context): AppIcon {
    val pm = context.packageManager
    return AppIcon.entries.firstOrNull {
      pm.getComponentEnabledSetting(component(context, it)) ==
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    } ?: AppIcon.DEFAULT
  }

  fun setIcon(
    activity: Activity,
    icon: AppIcon,
    @StyleRes defaultSplashTheme: Int
  ) {
    val pm = activity.packageManager

    // Applied on the next cold start
    activity.splashScreen.setSplashScreenTheme(icon.splashTheme ?: defaultSplashTheme)

    // Enable the new alias first so there's never zero launcher entries
    pm.setComponentEnabledSetting(
      component(activity, icon),
      PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
      PackageManager.DONT_KILL_APP
    )

    AppIcon.entries
      .filter { it != icon }
      .forEach {
        pm.setComponentEnabledSetting(
          component(activity, it),
          PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP
        )
      }
  }

  private fun component(context: Context, icon: AppIcon) =
    ComponentName(context.packageName, "${context.packageName}${icon.aliasName}")
}
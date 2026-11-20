package com.github.devlotfi.cipherbox.shared.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val mqtt: ImageVector
  get() {
    if (_mqtt != null) return _mqtt!!
    _mqtt = ImageVector.Builder(
      name = "Mqtt",
      defaultWidth = 203.dp,
      defaultHeight = 203.dp,
      viewportWidth = 203f,
      viewportHeight = 203f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF660066)),
      ) {
        moveTo(0.6f, 115f)
        horizontalLineTo(0f)
        verticalLineTo(192.6f)
        curveTo(0f, 198.2f, 4.6f, 202.8f, 10.2f, 202.8f)
        horizontalLineTo(90.1f)
        curveTo(89.4f, 154.3f, 49.6f, 115f, 0.6f, 115f)
        close()
        moveTo(0.6f, 51f)
        horizontalLineTo(0f)
        verticalLineTo(84f)
        curveTo(65.9f, 84.3f, 119.5f, 137.3f, 120.2f, 202.8f)
        horizontalLineTo(154.4f)
        curveTo(153.8f, 119f, 85.1f, 51f, 0.6f, 51f)
        close()
        moveTo(202.9f, 192.7f)
        verticalLineTo(122.6f)
        curveTo(179.7f, 65.8f, 133f, 20.9f, 74.9f, 0f)
        horizontalLineTo(10.2f)
        curveTo(4.6f, 0f, 0f, 4.6f, 0f, 10.2f)
        verticalLineTo(20.1f)
        curveTo(101.4f, 20.4f, 183.9f, 102.1f, 184.5f, 202.9f)
        horizontalLineTo(192.7f)
        curveTo(198.4f, 202.8f, 202.9f, 198.3f, 202.9f, 192.7f)
        close()
        moveTo(176.2f, 28.3f)
        curveTo(185.6f, 37.7f, 195.2f, 49.6f, 202.9f, 60.1f)
        verticalLineTo(10.1f)
        curveTo(202.904f, 8.773f, 202.645f, 7.457f, 202.139f, 6.23f)
        curveTo(201.633f, 5.003f, 200.889f, 3.888f, 199.951f, 2.949f)
        curveTo(199.012f, 2.011f, 197.897f, 1.267f, 196.67f, 0.761f)
        curveTo(195.443f, 0.255f, 194.127f, -0.004f, 192.8f, 0f)
        horizontalLineTo(141.3f)
        curveTo(153.3f, 8.3f, 165.7f, 17.8f, 176.2f, 28.3f)
        close()
      }
    }.build()
    return _mqtt!!
  }

private var _mqtt: ImageVector? = null

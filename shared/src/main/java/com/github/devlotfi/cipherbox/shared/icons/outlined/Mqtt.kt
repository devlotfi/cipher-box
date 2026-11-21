package com.github.devlotfi.cipherbox.shared.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val mqtt: ImageVector
  get() {
    if (_Mqtt != null) {
      return _Mqtt!!
    }
    _Mqtt = ImageVector.Builder(
      name = "Mqtt",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(2.059f, 13.336f)
        horizontalLineTo(2f)
        verticalLineTo(20.985f)
        curveTo(2f, 21.537f, 2.453f, 21.99f, 3.005f, 21.99f)
        horizontalLineTo(10.881f)
        curveTo(10.812f, 17.209f, 6.889f, 13.336f, 2.059f, 13.336f)
        close()
        moveTo(2.059f, 7.027f)
        horizontalLineTo(2f)
        verticalLineTo(10.28f)
        curveTo(8.496f, 10.309f, 13.779f, 15.534f, 13.848f, 21.99f)
        horizontalLineTo(17.219f)
        curveTo(17.16f, 13.73f, 10.388f, 7.027f, 2.059f, 7.027f)
        close()
        moveTo(22f, 20.995f)
        verticalLineTo(14.085f)
        curveTo(19.713f, 8.486f, 15.11f, 4.06f, 9.383f, 2f)
        horizontalLineTo(3.005f)
        curveTo(2.453f, 2f, 2f, 2.453f, 2f, 3.005f)
        verticalLineTo(3.981f)
        curveTo(11.995f, 4.011f, 20.127f, 12.064f, 20.186f, 22f)
        horizontalLineTo(20.995f)
        curveTo(21.556f, 21.99f, 22f, 21.547f, 22f, 20.995f)
        close()
        moveTo(19.368f, 4.79f)
        curveTo(20.295f, 5.716f, 21.241f, 6.889f, 22f, 7.924f)
        verticalLineTo(2.996f)
        curveTo(22f, 2.865f, 21.975f, 2.735f, 21.925f, 2.614f)
        curveTo(21.875f, 2.493f, 21.802f, 2.383f, 21.709f, 2.291f)
        curveTo(21.617f, 2.198f, 21.507f, 2.125f, 21.386f, 2.075f)
        curveTo(21.265f, 2.025f, 21.135f, 2f, 21.004f, 2f)
        horizontalLineTo(15.928f)
        curveTo(17.111f, 2.818f, 18.333f, 3.755f, 19.368f, 4.79f)
        close()
      }
    }.build()

    return _Mqtt!!
  }

@Suppress("ObjectPropertyName")
private var _Mqtt: ImageVector? = null

package com.github.devlotfi.cipherbox.shared.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val bluetooth_disabled: ImageVector
  get() {
    if (_bluetooth_disabled != null) {
      return _bluetooth_disabled!!
    }
    _bluetooth_disabled =
      ImageVector.Builder(
        name = "bluetooth_disabled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11f, 14.4f)
            lineTo(7.1f, 18.3f)
            quadTo(6.83f, 18.58f, 6.4f, 18.58f)
            reflectiveQuadTo(5.7f, 18.3f)
            quadTo(5.43f, 18.02f, 5.43f, 17.6f)
            reflectiveQuadTo(5.7f, 16.9f)
            lineTo(9.9f, 12.7f)
            lineTo(2.1f, 4.9f)
            quadTo(1.83f, 4.63f, 1.83f, 4.2f)
            reflectiveQuadTo(2.1f, 3.5f)
            quadTo(2.38f, 3.22f, 2.8f, 3.22f)
            reflectiveQuadTo(3.5f, 3.5f)
            lineToRelative(17f, 17f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadTo(20.5f, 21.9f)
            quadToRelative(-0.27f, 0.28f, -0.7f, 0.28f)
            reflectiveQuadTo(19.1f, 21.9f)
            lineTo(15.6f, 18.4f)
            lineToRelative(-2.9f, 2.9f)
            quadToRelative(-0.15f, 0.15f, -0.33f, 0.22f)
            reflectiveQuadTo(12f, 21.6f)
            quadToRelative(-0.4f, 0f, -0.7f, -0.29f)
            reflectiveQuadTo(11f, 20.58f)
            verticalLineTo(14.4f)
            close()
            moveToRelative(2f, 3.75f)
            lineTo(14.15f, 17f)
            lineTo(13f, 15.85f)
            verticalLineToRelative(2.3f)
            close()
            moveTo(14.1f, 11.3f)
            lineTo(12.7f, 9.9f)
            lineTo(14.9f, 7.7f)
            lineTo(13f, 5.85f)
            verticalLineTo(10.2f)
            lineToRelative(-2f, -2f)
            verticalLineTo(3.42f)
            quadTo(11f, 2.97f, 11.3f, 2.69f)
            reflectiveQuadTo(12f, 2.4f)
            quadToRelative(0.2f, 0f, 0.38f, 0.07f)
            reflectiveQuadTo(12.7f, 2.7f)
            lineTo(17f, 7f)
            quadToRelative(0.15f, 0.15f, 0.21f, 0.32f)
            reflectiveQuadTo(17.28f, 7.7f)
            reflectiveQuadTo(17.21f, 8.07f)
            reflectiveQuadTo(17f, 8.4f)
            lineToRelative(-2.9f, 2.9f)
            close()
          }
        }
        .build()
    return _bluetooth_disabled!!
  }

private var _bluetooth_disabled: ImageVector? = null
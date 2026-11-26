package com.github.devlotfi.cipherbox.shared.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val chat_error: ImageVector
  get() {
    if (_chat_error != null) {
      return _chat_error!!
    }
    _chat_error =
      ImageVector.Builder(
        name = "chat_error",
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
            moveTo(6f, 18f)
            lineTo(3.7f, 20.3f)
            quadTo(3.23f, 20.78f, 2.61f, 20.51f)
            reflectiveQuadTo(2f, 19.58f)
            verticalLineTo(4f)
            quadTo(2f, 3.17f, 2.59f, 2.59f)
            reflectiveQuadTo(4f, 2f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(22f, 4f)
            verticalLineTo(16f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 18f)
            horizontalLineTo(6f)
            close()
            moveToRelative(6f, -6.6f)
            lineToRelative(1.9f, 1.9f)
            quadToRelative(0.28f, 0.28f, 0.7f, 0.28f)
            reflectiveQuadTo(15.3f, 13.3f)
            reflectiveQuadToRelative(0.28f, -0.7f)
            reflectiveQuadTo(15.3f, 11.9f)
            lineTo(13.4f, 10f)
            lineTo(15.3f, 8.1f)
            quadTo(15.58f, 7.82f, 15.58f, 7.4f)
            reflectiveQuadTo(15.3f, 6.7f)
            reflectiveQuadTo(14.6f, 6.43f)
            reflectiveQuadTo(13.9f, 6.7f)
            lineTo(12f, 8.6f)
            lineTo(10.1f, 6.7f)
            quadTo(9.83f, 6.43f, 9.4f, 6.43f)
            reflectiveQuadTo(8.7f, 6.7f)
            reflectiveQuadTo(8.43f, 7.4f)
            reflectiveQuadTo(8.7f, 8.1f)
            lineTo(10.6f, 10f)
            lineTo(8.7f, 11.9f)
            quadTo(8.43f, 12.18f, 8.43f, 12.6f)
            quadToRelative(0f, 0.42f, 0.28f, 0.7f)
            reflectiveQuadToRelative(0.7f, 0.28f)
            reflectiveQuadTo(10.1f, 13.3f)
            lineTo(12f, 11.4f)
            close()
          }
        }
        .build()
    return _chat_error!!
  }

private var _chat_error: ImageVector? = null
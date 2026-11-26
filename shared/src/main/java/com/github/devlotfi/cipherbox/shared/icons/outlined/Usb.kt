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
public val usb: ImageVector
  get() {
    if (_usb != null) {
      return _usb!!
    }
    _usb =
      ImageVector.Builder(
        name = "usb",
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
            moveTo(10.59f, 21.41f)
            quadTo(10f, 20.83f, 10f, 20f)
            quadToRelative(0f, -0.52f, 0.28f, -0.98f)
            quadTo(10.55f, 18.58f, 11f, 18.3f)
            verticalLineTo(16f)
            horizontalLineTo(8f)
            quadTo(7.18f, 16f, 6.59f, 15.41f)
            reflectiveQuadTo(6f, 14f)
            verticalLineTo(11.7f)
            quadTo(5.55f, 11.48f, 5.28f, 11.02f)
            quadTo(5f, 10.58f, 5f, 10f)
            quadTo(5f, 9.17f, 5.59f, 8.59f)
            reflectiveQuadTo(7f, 8f)
            quadTo(7.83f, 8f, 8.41f, 8.59f)
            reflectiveQuadTo(9f, 10f)
            quadToRelative(0f, 0.57f, -0.28f, 1f)
            reflectiveQuadTo(8f, 11.7f)
            verticalLineTo(14f)
            horizontalLineToRelative(3f)
            verticalLineTo(6f)
            horizontalLineTo(10f)
            quadTo(9.7f, 6f, 9.55f, 5.72f)
            reflectiveQuadTo(9.6f, 5.2f)
            lineToRelative(2f, -2.68f)
            quadTo(11.75f, 2.32f, 12f, 2.32f)
            reflectiveQuadToRelative(0.4f, 0.2f)
            lineToRelative(2f, 2.68f)
            quadToRelative(0.2f, 0.25f, 0.05f, 0.52f)
            reflectiveQuadTo(14f, 6f)
            horizontalLineTo(13f)
            verticalLineToRelative(8f)
            horizontalLineToRelative(3f)
            verticalLineTo(12f)
            horizontalLineTo(15.5f)
            quadToRelative(-0.2f, 0f, -0.35f, -0.15f)
            reflectiveQuadTo(15f, 11.5f)
            verticalLineToRelative(-3f)
            quadTo(15f, 8.3f, 15.15f, 8.15f)
            reflectiveQuadTo(15.5f, 8f)
            horizontalLineToRelative(3f)
            quadToRelative(0.2f, 0f, 0.35f, 0.15f)
            reflectiveQuadTo(19f, 8.5f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.2f, -0.15f, 0.35f)
            reflectiveQuadTo(18.5f, 12f)
            horizontalLineTo(18f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(16f, 16f)
            horizontalLineTo(13f)
            verticalLineToRelative(2.3f)
            quadToRelative(0.48f, 0.25f, 0.74f, 0.7f)
            reflectiveQuadTo(14f, 20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(12f, 22f)
            reflectiveQuadTo(10.59f, 21.41f)
            close()
          }
        }
        .build()
    return _usb!!
  }

private var _usb: ImageVector? = null
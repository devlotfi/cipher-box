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
public val widgets: ImageVector
  get() {
    if (_widgets != null) {
      return _widgets!!
    }
    _widgets =
      ImageVector.Builder(
        name = "widgets",
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
            moveTo(15.95f, 12.3f)
            lineTo(11.7f, 8.05f)
            quadTo(11.55f, 7.9f, 11.49f, 7.72f)
            reflectiveQuadTo(11.43f, 7.35f)
            reflectiveQuadTo(11.49f, 6.97f)
            reflectiveQuadTo(11.7f, 6.65f)
            lineTo(15.95f, 2.4f)
            quadTo(16.1f, 2.25f, 16.28f, 2.19f)
            reflectiveQuadTo(16.65f, 2.13f)
            reflectiveQuadToRelative(0.38f, 0.06f)
            reflectiveQuadTo(17.35f, 2.4f)
            lineTo(21.6f, 6.65f)
            quadToRelative(0.15f, 0.15f, 0.21f, 0.32f)
            reflectiveQuadToRelative(0.06f, 0.38f)
            reflectiveQuadTo(21.81f, 7.72f)
            reflectiveQuadTo(21.6f, 8.05f)
            lineTo(17.35f, 12.3f)
            quadToRelative(-0.15f, 0.15f, -0.32f, 0.21f)
            reflectiveQuadToRelative(-0.38f, 0.06f)
            reflectiveQuadTo(16.28f, 12.51f)
            reflectiveQuadTo(15.95f, 12.3f)
            close()
            moveTo(3f, 10f)
            verticalLineTo(4f)
            quadTo(3f, 3.57f, 3.29f, 3.29f)
            reflectiveQuadTo(4f, 3f)
            horizontalLineToRelative(6f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(11f, 4f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(10f, 11f)
            horizontalLineTo(4f)
            quadTo(3.58f, 11f, 3.29f, 10.71f)
            quadTo(3f, 10.43f, 3f, 10f)
            close()
            moveTo(13f, 20f)
            verticalLineTo(14f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(14f, 13f)
            horizontalLineToRelative(6f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 14f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(20f, 21f)
            horizontalLineTo(14f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(13f, 20.43f, 13f, 20f)
            close()
            moveTo(3f, 20f)
            verticalLineTo(14f)
            quadTo(3f, 13.58f, 3.29f, 13.29f)
            reflectiveQuadTo(4f, 13f)
            horizontalLineToRelative(6f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(11f, 14f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(10f, 21f)
            horizontalLineTo(4f)
            quadTo(3.58f, 21f, 3.29f, 20.71f)
            quadTo(3f, 20.43f, 3f, 20f)
            close()
            moveTo(5f, 9f)
            horizontalLineTo(9f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(9f)
            close()
            moveToRelative(11.68f, 1.2f)
            lineTo(19.5f, 7.38f)
            lineTo(16.68f, 4.55f)
            lineTo(13.85f, 7.38f)
            lineToRelative(2.82f, 2.82f)
            close()
            moveTo(15f, 19f)
            horizontalLineToRelative(4f)
            verticalLineTo(15f)
            horizontalLineTo(15f)
            verticalLineToRelative(4f)
            close()
            moveTo(5f, 19f)
            horizontalLineTo(9f)
            verticalLineTo(15f)
            horizontalLineTo(5f)
            verticalLineToRelative(4f)
            close()
            moveTo(9f, 9f)
            close()
            moveTo(13.85f, 7.38f)
            close()
            moveTo(9f, 15f)
            close()
            moveToRelative(6f, 0f)
            close()
          }
        }
        .build()
    return _widgets!!
  }

private var _widgets: ImageVector? = null
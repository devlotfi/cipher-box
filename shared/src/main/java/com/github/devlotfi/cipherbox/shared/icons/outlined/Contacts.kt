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
public val contacts: ImageVector
  get() {
    if (_contacts != null) {
      return _contacts!!
    }
    _contacts =
      ImageVector.Builder(
        name = "contacts",
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
            moveTo(5f, 23f)
            quadTo(4.58f, 23f, 4.29f, 22.71f)
            quadTo(4f, 22.43f, 4f, 22f)
            reflectiveQuadTo(4.29f, 21.29f)
            reflectiveQuadTo(5f, 21f)
            horizontalLineTo(19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 22f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(19f, 23f)
            horizontalLineTo(5f)
            close()
            moveTo(5f, 3f)
            quadTo(4.58f, 3f, 4.29f, 2.71f)
            reflectiveQuadTo(4f, 2f)
            quadTo(4f, 1.57f, 4.29f, 1.29f)
            reflectiveQuadTo(5f, 1f)
            horizontalLineTo(19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 2f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(19f, 3f)
            horizontalLineTo(5f)
            close()
            moveToRelative(7f, 10f)
            quadToRelative(1.25f, 0f, 2.13f, -0.88f)
            reflectiveQuadTo(15f, 10f)
            reflectiveQuadTo(14.13f, 7.88f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(9.88f, 7.88f)
            reflectiveQuadTo(9f, 10f)
            reflectiveQuadToRelative(0.88f, 2.13f)
            reflectiveQuadTo(12f, 13f)
            close()
            moveTo(4f, 20f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 5.18f, 22f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 20f)
            horizontalLineTo(4f)
            close()
            moveTo(5.75f, 18f)
            quadTo(6.88f, 16.6f, 8.48f, 15.8f)
            quadTo(10.08f, 15f, 12f, 15f)
            reflectiveQuadToRelative(3.53f, 0.8f)
            reflectiveQuadTo(18.25f, 18f)
            horizontalLineTo(20f)
            verticalLineTo(6f)
            horizontalLineTo(4f)
            verticalLineTo(18f)
            horizontalLineTo(5.75f)
            close()
            moveTo(8.7f, 18f)
            horizontalLineToRelative(6.6f)
            quadTo(14.58f, 17.5f, 13.74f, 17.25f)
            reflectiveQuadTo(12f, 17f)
            reflectiveQuadToRelative(-1.74f, 0.25f)
            reflectiveQuadTo(8.7f, 18f)
            close()
            moveToRelative(2.59f, -7.29f)
            quadTo(11f, 10.43f, 11f, 10f)
            quadTo(11f, 9.57f, 11.29f, 9.29f)
            reflectiveQuadTo(12f, 9f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 10f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(12f, 11f)
            reflectiveQuadTo(11.29f, 10.71f)
            close()
            moveTo(12f, 12f)
            close()
          }
        }
        .build()
    return _contacts!!
  }

private var _contacts: ImageVector? = null
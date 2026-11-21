package com.github.devlotfi.cipherbox.shared.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val tor: ImageVector
  get() {
    if (_Tor != null) {
      return _Tor!!
    }
    _Tor = ImageVector.Builder(
      name = "Tor",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd
      ) {
        moveTo(12.021f, 20.181f)
        verticalLineTo(18.969f)
        curveTo(15.86f, 18.958f, 18.97f, 15.842f, 18.97f, 12f)
        curveTo(18.97f, 8.157f, 15.86f, 5.042f, 12.021f, 5.031f)
        verticalLineTo(3.818f)
        curveTo(16.53f, 3.83f, 20.181f, 7.488f, 20.181f, 12f)
        curveTo(20.181f, 16.512f, 16.53f, 20.17f, 12.021f, 20.181f)
        close()
        moveTo(12.021f, 15.938f)
        curveTo(14.186f, 15.927f, 15.939f, 14.169f, 15.939f, 12f)
        curveTo(15.939f, 9.831f, 14.186f, 8.073f, 12.021f, 8.061f)
        verticalLineTo(6.849f)
        curveTo(14.856f, 6.861f, 17.152f, 9.162f, 17.152f, 12f)
        curveTo(17.152f, 14.838f, 14.856f, 17.139f, 12.021f, 17.15f)
        verticalLineTo(15.938f)
        close()
        moveTo(12.021f, 9.88f)
        curveTo(13.182f, 9.891f, 14.121f, 10.835f, 14.121f, 12f)
        curveTo(14.121f, 13.165f, 13.182f, 14.109f, 12.021f, 14.12f)
        verticalLineTo(9.88f)
        close()
        moveTo(2f, 12f)
        curveTo(2f, 17.523f, 6.477f, 22f, 12f, 22f)
        curveTo(17.523f, 22f, 22f, 17.523f, 22f, 12f)
        curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
        curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
        close()
      }
    }.build()

    return _Tor!!
  }

@Suppress("ObjectPropertyName")
private var _Tor: ImageVector? = null

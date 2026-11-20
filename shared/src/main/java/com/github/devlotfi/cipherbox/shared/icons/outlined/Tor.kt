package com.github.devlotfi.cipherbox.shared.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val tor: ImageVector
  get() {
    if (_tor != null) return _tor!!
    _tor = ImageVector.Builder(
      name = "Tor",
      defaultWidth = 180.dp,
      defaultHeight = 180.dp,
      viewportWidth = 180f,
      viewportHeight = 180f,
    ).apply {
      path(
        fill = SolidColor(Color(0xFF000000)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(90.185f, 163.631f)
        lineTo(90.185f, 152.721f)
        curveTo(124.744f, 152.621f, 152.726f, 124.581f, 152.726f, 89.998f)
        curveTo(152.726f, 55.416f, 124.744f, 27.376f, 90.185f, 27.276f)
        lineTo(90.185f, 16.366f)
        curveTo(130.769f, 16.469f, 163.634f, 49.391f, 163.634f, 89.998f)
        curveTo(163.634f, 130.607f, 130.769f, 163.531f, 90.185f, 163.631f)
        close()
        moveTo(90.185f, 125.445f)
        curveTo(109.677f, 125.342f, 125.455f, 109.517f, 125.455f, 89.998f)
        curveTo(125.455f, 70.48f, 109.677f, 54.655f, 90.185f, 54.553f)
        lineTo(90.185f, 43.645f)
        curveTo(115.705f, 43.745f, 136.365f, 64.455f, 136.365f, 89.998f)
        curveTo(136.365f, 115.542f, 115.705f, 136.252f, 90.185f, 136.352f)
        lineTo(90.185f, 125.445f)
        close()
        moveTo(90.185f, 70.917f)
        curveTo(100.641f, 71.017f, 109.091f, 79.517f, 109.091f, 89.998f)
        curveTo(109.091f, 100.481f, 100.641f, 108.981f, 90.185f, 109.081f)
        lineTo(90.185f, 70.917f)
        close()
        moveTo(0f, 89.998f)
        curveTo(0f, 139.705f, 40.292f, 180f, 90f, 180f)
        curveTo(139.705f, 180f, 180f, 139.705f, 180f, 89.998f)
        curveTo(180f, 40.292f, 139.705f, 0f, 90f, 0f)
        curveTo(40.292f, 0f, 0f, 40.292f, 0f, 89.998f)
        close()
      }
    }.build()
    return _tor!!
  }

private var _tor: ImageVector? = null

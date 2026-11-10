package com.github.devlotfi.cipherbox.shared.composables

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.RoundedPolygon.Companion as Poly

fun defaultLoadingPolygons(): List<RoundedPolygon> = listOf(
  Poly.star(
    numVerticesPerRadius = 9, innerRadius = 0.8f,
    rounding = CornerRounding(0.5f), // 9-sided cookie
  ),
  RoundedPolygon(
    numVertices = 3,
    rounding = CornerRounding(0.3f), // rounded triangle
  ),
  Poly.star(
    numVerticesPerRadius = 5, innerRadius = 0.85f,
    rounding = CornerRounding(0.35f), // rounded pentagon
  ),
  Poly.rectangle(
    width = 1.7f, height = 0.85f,
    rounding = CornerRounding(0.42f), // pill
  ),
  Poly.star(
    numVerticesPerRadius = 12, innerRadius = 0.82f,
    rounding = CornerRounding(0.12f), // sunny
  ),
).map { it.normalized() }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PolygonLoadingIndicator() {
  LoadingIndicator(
    polygons = defaultLoadingPolygons()
  )
}
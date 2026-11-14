package com.github.devlotfi.cipherbox.shared.utils

data class RoomFlowData<T>(
  val loading: Boolean,
  val data: T? = null
)
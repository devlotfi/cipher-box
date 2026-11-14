package com.github.devlotfi.cipherbox.shared.global

import com.devlotfi.kanyon.schema.FieldSchema
import com.github.devlotfi.composeform.FormDefinition

abstract class KanyonFormDefinition(
  parent: FormDefinition<FieldSchema>? = null,
  validateOnChange: Boolean = true,
  validateOnBlur: Boolean = true
) : FormDefinition<FieldSchema>(
  validateOnChange = validateOnChange,
  validateOnBlur = validateOnBlur,
  root = parent
) {
  override fun <T> validateField(value: T, schema: FieldSchema?): String? {
    return schema?.validate(value)
  }
}
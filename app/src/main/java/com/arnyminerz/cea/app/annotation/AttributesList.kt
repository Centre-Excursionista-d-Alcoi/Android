package com.arnyminerz.cea.app.annotation

import androidx.annotation.StringDef

@Target(AnnotationTarget.TYPE)
@StringDef(ATTR_BRAND, ATTR_COLOR, ATTR_LENGTH)
annotation class Attribute

const val ATTR_BRAND = "brand"
const val ATTR_COLOR = "color"
const val ATTR_LENGTH = "length"

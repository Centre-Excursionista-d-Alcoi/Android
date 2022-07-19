package com.arnyminerz.cea.app.utils

import androidx.annotation.StringRes
import com.arnyminerz.cea.app.R

class LocalizableColor(colorName: String) {
    @StringRes
    val string: Int = when (colorName) {
        "black" -> R.string.color_black
        "red" -> R.string.color_red
        "blue" -> R.string.color_blue
        "green" -> R.string.color_green
        "yellow" -> R.string.color_yellow
        "purple" -> R.string.color_purple
        "pink" -> R.string.color_pink
        "orange" -> R.string.color_orange
        "gray" -> R.string.color_gray
        else -> R.string.color_unknown
    }
}
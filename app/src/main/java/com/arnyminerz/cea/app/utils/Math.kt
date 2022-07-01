package com.arnyminerz.cea.app.utils

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.ui.unit.Dp

val Dp.toPx get() = this.value.toPx

val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics,
    )

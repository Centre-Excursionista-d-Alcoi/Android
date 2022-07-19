package com.arnyminerz.cea.app.utils

import android.os.Build
import android.os.Bundle
import kotlin.reflect.KClass

fun <T : Any> Bundle.getParcelableCompat(key: String, kClass: KClass<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelable(key, kClass.java)
    else
        getParcelable(key)

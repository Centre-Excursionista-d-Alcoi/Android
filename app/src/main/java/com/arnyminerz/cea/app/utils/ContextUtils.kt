package com.arnyminerz.cea.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.arnyminerz.cea.app.annotation.ToastDuration
import kotlin.reflect.KClass

fun Context.launchUrl(url: String) =
    startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
    )

/**
 * Shortcut for [Context.startActivity]. Initializes a new [Intent] with the current context as
 * [Context], and the target [activity] as the activity to launch. Also Applies
 */
fun <A : Activity> Context.launch(activity: KClass<A>, builder: Intent.() -> Unit = {}) =
    startActivity(
        Intent(this, activity.java).apply(builder)
    )

fun Context.toast(text: String, @ToastDuration duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

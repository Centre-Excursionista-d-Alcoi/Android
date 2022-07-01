package com.arnyminerz.cea.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.launchUrl(url: String) =
    startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
    )

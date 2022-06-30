package com.arnyminerz.lib.app_intro.action

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Sets the data for an action for an intro page.
 * @author Arnau Mora
 * @since 20220130
 */
data class IntroAction<in R : Any?>(
    val text: String,
    val currentValue: MutableState<@UnsafeVariance R>,
    val type: IntroActionType<@UnsafeVariance R>,
    val callback: IntroActionContext<@UnsafeVariance R>.(value: R) -> Unit,
    val enabled: Boolean = true,
) {
    companion object {
        /**
         * The default value for [IntroAction] that will display no actions.
         * @author Arnau Mora
         * @since 20220130
         */
        val None = IntroAction("", mutableStateOf(null), IntroActionType.NONE, {})
    }
}

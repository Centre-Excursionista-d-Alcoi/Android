package com.arnyminerz.cea.app.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Runs a block of code in the IO ([Dispatchers.IO]) context.
 * @author Arnau Mora
 * @since 20220719
 * @param block The block of code to run.
 */
suspend fun <R> io(block: suspend CoroutineScope.() -> R) = withContext(Dispatchers.IO) { block() }

/**
 * Runs a block of code in the UI ([Dispatchers.Main]) context.
 * @author Arnau Mora
 * @since 20220719
 * @param block The block of code to run.
 */
suspend fun <R> ui(block: suspend CoroutineScope.() -> R) =
    withContext(Dispatchers.Main) { block() }

package com.carloshinojosa.idealistachallenge.core.data.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/** Abstracts coroutine dispatchers so tests can inject a test dispatcher. */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

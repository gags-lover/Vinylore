package com.github.astat1cc.vinylore.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {

    fun io(): CoroutineDispatcher

    fun default(): CoroutineDispatcher

    class Impl : DispatchersProvider {

        override fun io(): CoroutineDispatcher = Dispatchers.IO

        override fun default(): CoroutineDispatcher = Dispatchers.Default
    }
}
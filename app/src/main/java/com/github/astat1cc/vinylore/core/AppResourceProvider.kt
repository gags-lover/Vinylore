package com.github.astat1cc.vinylore.core

import android.content.Context
import androidx.annotation.StringRes

interface AppResourceProvider {

    fun getString(@StringRes id: Int): String

    class Impl(private val context: Context) : AppResourceProvider {

        override fun getString(id: Int): String = context.getString(id)
    }
}
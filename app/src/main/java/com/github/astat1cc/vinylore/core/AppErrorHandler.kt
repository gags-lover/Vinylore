package com.github.astat1cc.vinylore.core

import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.domain.ErrorType

interface AppErrorHandler {

    fun getErrorMessage(error: ErrorType): String

    fun getErrorTypeOf(exception: Exception): ErrorType

    class Impl(
        private val resources: AppResourceProvider
    ) : AppErrorHandler {

        override fun getErrorMessage(error: ErrorType): String =
            when (error) {
                ErrorType.GENERIC -> resources.getString(R.string.something_went_wrong)
            }

        override fun getErrorTypeOf(exception: Exception): ErrorType = ErrorType.GENERIC
    }
}
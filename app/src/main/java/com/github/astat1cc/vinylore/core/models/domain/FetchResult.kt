package com.github.astat1cc.vinylore.core.models.domain

sealed class FetchResult<T> {

    data class Success<T>(val data: T?) : FetchResult<T>()

    data class Fail<T>(val error: ErrorType) : FetchResult<T>()
}
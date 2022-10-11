package com.github.astat1cc.vinylore.core.models.domain

sealed class FetchResult<T> {

    class Success<T>(val data: T?) : FetchResult<T>()

    class Fail<T>(val error: ErrorType) : FetchResult<T>()
}
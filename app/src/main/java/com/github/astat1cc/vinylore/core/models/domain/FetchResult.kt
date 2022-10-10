package com.github.astat1cc.vinylore.core.models.domain

sealed class FetchResult {

    class Success(val albums: List<AppAlbum>?) : FetchResult()

    class Fail(val error: ErrorType) : FetchResult()
}
package com.github.astat1cc.vinylore.player.ui.util

import android.util.Log

//interface TrackNameConverterUtils {
//
//    fun convertTrackNameToVinylTrackListStyle(name: String): String
//
//    fun removeUnderscoresAndPathSegment(name: String): String
//
//    class Impl : TrackNameConverterUtils {

internal fun String.removeUnderscoresAndPathSegment(): String =
    this.replace("_", " ")
        .replaceAfterLast(".", "")
        .dropLast(1)

//internal fun String.convertTrackNameToVinylTrackListStyle(): String =
//    this.removeUnderscoresAndPathSegment().uppercase()
//    }
//}
package com.github.astat1cc.vinylore.albumlist.ui.views

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.models.ui.ListingAlbumUi
import com.github.astat1cc.vinylore.core.theme.brownForGradient
import com.github.astat1cc.vinylore.player.ui.views.dpToSp

@Composable
fun AlbumView(
    album: ListingAlbumUi,
    onClick: (Uri) -> Unit,
    clickedAlbumUri: Uri?,
    screenHeight: Int,
    isPlayingNow: Boolean
) {
    // todo remove discs if isPlayingNow
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (isPlayingNow) brownForGradient else Color.Transparent)
            .clickable {
                onClick(album.uri)
            }
            .padding(vertical = 20.dp, horizontal = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 160.dp, height = 128.dp)
                .zIndex(1f)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = clickedAlbumUri == null,
                exit =
//                shrinkOut(
//                    animationSpec = tween(
//                        durationMillis = AppConst.SLIDE_OUT_DURATION,
//                        easing = FastOutLinearInEasing,
//                    ),
//                    clip = false,
//                    targetSize = { IntSize(screenWidth, -screenWidth * 3) }
//                )
                if (clickedAlbumUri == album.uri) {
                    slideOutVertically(
                        targetOffsetY = { -screenHeight },
                        animationSpec = tween(
                            durationMillis = AppConst.SLIDE_OUT_DURATION + 100,
                            easing = LinearEasing,
                            delayMillis = 250 // FADE_OUT_DURATION + 50 millis just in case
                        )
                    )
                } else {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = AppConst.FADE_OUT_DURATION,
                            easing = LinearEasing
                        )
                    )
                }
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Image(
                        painter = painterResource(R.drawable.album_in_list),
                        contentDescription = null,
                        modifier = Modifier
                            .width(160.dp)
                            .height(128.dp),
                        contentScale = ContentScale.FillWidth,
                    )
                    Text(
                        text = "${album.trackCount} track${if (album.trackCount > 1) "s" else ""}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(end = 48.dp, start = 4.dp),
                        textAlign = TextAlign.Center,
                        fontSize = dpToSp(dp = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(
            text = album.name ?: stringResource(id = R.string.unknown_name),
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis
        )
    }
}
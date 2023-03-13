package com.github.astat1cc.vinylore.player.ui.views.vinyl

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.theme.steelGray

@Composable
fun VinylView(
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f,
    albumCover: Bitmap?
) {
    Box(
        modifier = modifier
            .aspectRatio(1.0f)
            .clip(VinylShape())
    ) {
        // Vinyl background
        Image(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationDegrees),
            painter = painterResource(id = R.drawable.vinyl_background),
            contentDescription = ""
        )

        // Vinyl lights effect
        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(id = R.drawable.vinyl_light),
            contentDescription = "",
            alpha = 0.8f // todo prepare image
        )

        // Vinyl 'album' cover
        // For using with Coil or Glide, wrap into surface with shape
        val imageModifier = Modifier
            .fillMaxSize(0.4f)
            .align(Alignment.Center)
            .rotate(rotationDegrees)
            .aspectRatio(1.0f)
            .clip(CircleShape)
        if (albumCover != null) {
            Image(
                modifier = imageModifier,
                bitmap = albumCover.asImageBitmap(),
                contentDescription = null // todo content description
            )
        } else {
            Image(
                modifier = imageModifier,
                painter = painterResource(R.drawable.album_cover_vinylore),
                contentDescription = null // todo content description
            )
        }
    }
}


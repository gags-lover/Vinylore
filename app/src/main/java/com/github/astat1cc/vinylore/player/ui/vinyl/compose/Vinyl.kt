package com.github.astat1cc.vinylore.player.ui.vinyl.compose

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.theme.steelBlack
import com.github.astat1cc.vinylore.core.theme.steelGray
import com.github.astat1cc.vinylore.player.ui.vinyl.shape.VinylShape

@Preview
@Composable
fun Vinyl(
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f
) {
    Box(
        modifier = modifier
            .aspectRatio(1.0f)
            .clip(VinylShape())
    ) {

        // Vinyl platter
//        Box(
//            modifier = Modifier
////                .padding(4.dp)
//                .padding(6.dp)
//                .clip(CircleShape)
//                .fillMaxSize()
//                .border(width = 4.dp, color = steelGray, shape = CircleShape)
//                .background(Color.Black),
////            painter = painterResource(id = R.drawable.matte),
////            contentDescription = null,
////            contentScale = ContentScale.Crop,
////            alpha = 0.4f
//        )

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
            alpha = 0.8f
        )

        // Vinyl 'album' cover
        // For using with Coil or Glide, wrap into surface with shape 
        Image(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .align(Alignment.Center)
                .rotate(rotationDegrees)
                .aspectRatio(1.0f)
                .clip(CircleShape),
            painter = painterResource(R.drawable.album_cover_vinylore),
            contentDescription = null // todo content description
        )
    }
}


package com.github.astat1cc.vinylore.albumlist.ui.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylShape

@Composable
fun VinylUnderMockupView(
    modifier: Modifier = Modifier,
    albumCover: Bitmap?
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
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.vinyl_under_mockup),
            contentDescription = ""
        )

        // Vinyl 'album' cover
        // For using with Coil or Glide, wrap into surface with shape
        val imageModifier = Modifier
            .fillMaxSize(0.4f)
            .align(Alignment.Center)
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
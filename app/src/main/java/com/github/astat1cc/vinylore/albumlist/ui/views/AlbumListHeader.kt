package com.github.astat1cc.vinylore.albumlist.ui.views

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.astat1cc.vinylore.R

@Composable
fun AlbumListHeader(
    refreshButtonListener: () -> Unit,
    getDirLauncher: ManagedActivityResultLauncher<Uri?, Uri?>
) {
    Box(Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.ic_change_folder),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(CircleShape)
                .size(48.dp)
                .align(Alignment.CenterStart)
                .clickable {
                    getDirLauncher.launch(null)
                }
                .padding(12.dp)
        )
        Text(
            text = stringResource(R.string.album_collection),
            color = Color.White,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 60.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_refresh),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(CircleShape)
                .size(48.dp)
                .align(Alignment.CenterEnd)
                .clickable {
                    refreshButtonListener()
                }
                .padding(12.dp)
        )
    }
}
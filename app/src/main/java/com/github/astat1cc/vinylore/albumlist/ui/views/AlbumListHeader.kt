package com.github.astat1cc.vinylore.albumlist.ui.views

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.theme.brown

@Composable
fun AlbumListHeader(
    refreshButtonListener: () -> Unit,
    backButtonListener: () -> Unit,
    getDirLauncher: ManagedActivityResultLauncher<Uri?, Uri?>,
    showRefreshButton: Boolean
) {
    var showPopup by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        // back button
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(4.dp)
                .clip(CircleShape)
                .clickable(onClick = {
                    backButtonListener()
                })
                .align(CenterStart)
                .size(48.dp)
                .padding(12.dp)
        )
        Text(
            text = stringResource(R.string.album_collection),
            color = Color.White,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
//                .weight(1f)
                .padding(vertical = 8.dp, horizontal = 60.dp)
                .fillMaxWidth()
                .align(Center),
//                .padding(horizontal = 60.dp),
            fontWeight = FontWeight.Bold
        )

        val dropdownMenuItems =
            if (showRefreshButton) {
                listOf(
                    AppDropdownMenuItem(
                        text = stringResource(R.string.refresh),
                        onClick = {
                            showPopup = false
                            refreshButtonListener()
                        },
                        iconRes = R.drawable.ic_refresh
                    ),
                    AppDropdownMenuItem(
                        text = stringResource(R.string.change_root),
                        onClick = {
                            getDirLauncher.launch(null)
                            showPopup = false
                        },
                        iconRes = R.drawable.ic_change_root_folder
                    )
                )
            } else {
                listOf(
                    AppDropdownMenuItem(
                        text = stringResource(R.string.change_root),
                        onClick = {
                            getDirLauncher.launch(null)
                            showPopup = false
                        },
                        iconRes = R.drawable.ic_change_root_folder
                    )
                )
            }
        // more button
        Box(modifier = Modifier.align(CenterEnd)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .size(48.dp)
                    .clickable {
                        showPopup = true
                    }

                    .padding(12.dp)
            )
            DropdownMenu(
                expanded = showPopup,
                onDismissRequest = { showPopup = false }
            ) {
                dropdownMenuItems.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        onClick = { item.onClick() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = CenterVertically,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = null,
                                    tint = brown,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .size(48.dp)
                                        .padding(12.dp)
                                )
                                Text(text = item.text)
                            }
                            if (index < dropdownMenuItems.size - 1) {
                                Divider(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AppDropdownMenuItem(val text: String, val onClick: () -> Unit, val iconRes: Int)
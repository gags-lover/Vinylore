package com.github.astat1cc.vinylore.albumlist.ui.views

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.astat1cc.vinylore.core.theme.vintagePaper

@Composable
fun ChoseDirectoryButton(
    messageText: String,
    buttonText: String,
    getDirLauncher: ManagedActivityResultLauncher<Uri?, Uri?>
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            text = messageText,
            color = Color.White,
            fontSize = 16.sp
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = vintagePaper),
            onClick = {
                getDirLauncher.launch(null)
            }) {
            Text(text = buttonText, color = Color.Black, fontSize = 18.sp)
        }
    }
}
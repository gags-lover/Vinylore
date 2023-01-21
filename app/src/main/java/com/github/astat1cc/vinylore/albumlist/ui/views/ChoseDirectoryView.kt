package com.github.astat1cc.vinylore.albumlist.ui.views

import android.content.Intent
import android.net.Uri
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
fun ChoseDirectoryView(
    messageText: String,
    buttonText: String,
    dirChosenListener: (Uri) -> Unit
) {
    val contentResolver = LocalContext.current.contentResolver
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    val getDirLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { chosenDirUri ->
            if (chosenDirUri == null) return@rememberLauncherForActivityResult
            contentResolver.takePersistableUriPermission(chosenDirUri, takeFlags)
            dirChosenListener(chosenDirUri)
        }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            text = messageText,
            color = vintagePaper,
            fontSize = 18.sp
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
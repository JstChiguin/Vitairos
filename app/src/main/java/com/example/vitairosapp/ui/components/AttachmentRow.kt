package com.example.vitairosapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.DangerBg
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.util.BitmapUtils
import com.example.vitairosapp.util.PdfUtils
import com.example.vitairosapp.util.formatSize
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun Attachment.fileAvailable(): Boolean = path?.let { File(it).exists() } == true

@Composable
fun AttachmentThumb(attachment: Attachment, modifier: Modifier = Modifier) {
    val available = attachment.fileAvailable()
    val bitmap by produceState<Bitmap?>(null, attachment.path) {
        value = if (available) withContext(Dispatchers.IO) {
            if (attachment.isPdf) PdfUtils.renderPage(attachment.path!!, 0, 160)
            else BitmapUtils.decodeSampled(attachment.path!!, 160)
        } else null
    }
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Mist),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        when {
            bmp != null -> Image(
                bmp.asImageBitmap(),
                contentDescription = "Miniatura del adjunto",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            attachment.isPdf -> Text("📄", fontSize = 22.sp)
            else -> Text("▧", color = Ink, fontSize = 20.sp)
        }
    }
}

@Composable
fun AttachmentRow(
    attachment: Attachment,
    onView: () -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    val available = attachment.fileAvailable()
    VCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AttachmentThumb(attachment)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    attachment.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                val meta = buildString {
                    append(attachment.source.label)
                    append(" · ")
                    append(formatSize(attachment.sizeBytes))
                    if (attachment.isImage && attachment.width > 0) append(" · ${attachment.width}×${attachment.height}")
                    if (attachment.isPdf && attachment.pages > 0) append(" · ${attachment.pages} pág.")
                }
                Text(meta, color = Muted, fontSize = 11.5.sp)
                if (attachment.isImage && attachment.originalSizeBytes > attachment.sizeBytes) {
                    Text("Optimizada desde ${formatSize(attachment.originalSizeBytes)}", color = Muted, fontSize = 11.5.sp)
                }
                if (attachment.source == AttachmentSource.DEMO) {
                    Text("Referencia simulada: sin archivo local", color = Muted, fontSize = 11.5.sp)
                } else if (!available) {
                    Text("Archivo local liberado tras la sincronización", color = Muted, fontSize = 11.5.sp)
                }
                if (available) {
                    TextButton(onClick = onView, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("Ver archivo", color = CopperDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (onRemove != null) {
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DangerBg)
                ) {
                    Text("✕", color = Danger, fontSize = 18.sp)
                }
            }
        }
    }
}

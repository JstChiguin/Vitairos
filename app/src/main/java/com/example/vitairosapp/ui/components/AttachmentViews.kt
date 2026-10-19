package com.example.vitairosapp.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vitairosapp.data.Attachment
import com.example.vitairosapp.data.AttachmentSource
import com.example.vitairosapp.data.BitmapUtils
import com.example.vitairosapp.data.PdfUtils
import com.example.vitairosapp.ui.formatSize
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.DangerBg
import com.example.vitairosapp.ui.theme.Gold
import com.example.vitairosapp.ui.theme.HeaderMuted
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.theme.Night
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

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

/** Visor a pantalla completa para revisar la legibilidad del documento. */
@Composable
fun AttachmentPreviewDialog(attachment: Attachment, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Night)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("VISTA PREVIA", color = HeaderMuted, fontSize = 12.sp, letterSpacing = 2.sp)
                    Text(
                        attachment.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                VButton("Cerrar", onDismiss, style = ButtonStyle.Secondary)
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val path = attachment.path
                when {
                    path == null || !File(path).exists() -> Text(
                        "El archivo ya no está disponible en el dispositivo",
                        color = HeaderMuted, modifier = Modifier.padding(24.dp)
                    )
                    attachment.isPdf -> PdfPager(path, attachment.pages)
                    else -> ZoomableImage(path)
                }
            }
            Text(
                "Verifica que el documento sea legible, corresponda al reporte y esté bien orientado.",
                color = Gold, fontSize = 14.sp, lineHeight = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ZoomableImage(path: String) {
    val bitmap by produceState<Bitmap?>(null, path) {
        value = withContext(Dispatchers.IO) { BitmapUtils.decodeSampled(path, 2048) }
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val state = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        offsetX = if (scale == 1f) 0f else offsetX + pan.x
        offsetY = if (scale == 1f) 0f else offsetY + pan.y
    }
    bitmap?.let {
        Image(
            it.asImageBitmap(),
            contentDescription = "Documento adjunto",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .transformable(state)
                .graphicsLayer(
                    scaleX = scale, scaleY = scale,
                    translationX = offsetX, translationY = offsetY
                )
        )
    }
}

@Composable
private fun PdfPager(path: String, pages: Int) {
    var page by remember { mutableIntStateOf(0) }
    var failed by remember { mutableStateOf(false) }
    val total = pages.coerceAtLeast(1)
    val bitmap by produceState<Bitmap?>(null, path, page) {
        value = withContext(Dispatchers.IO) { PdfUtils.renderPage(path, page, 1400) }
        failed = value == null
    }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val bmp = bitmap
            if (bmp != null) {
                Image(
                    bmp.asImageBitmap(),
                    contentDescription = "Página ${page + 1} del PDF",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (failed) {
                Text("No fue posible abrir el PDF", color = HeaderMuted)
            }
        }
        Row(
            Modifier.padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VButton("←", { if (page > 0) page-- }, style = ButtonStyle.Secondary, enabled = page > 0)
            Text("Página ${page + 1} de $total", color = Color.White, fontSize = 14.sp)
            VButton("→", { if (page < total - 1) page++ }, style = ButtonStyle.Secondary, enabled = page < total - 1)
        }
        Spacer(Modifier.height(4.dp))
    }
}

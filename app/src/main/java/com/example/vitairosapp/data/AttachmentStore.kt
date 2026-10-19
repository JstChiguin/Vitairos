package com.example.vitairosapp.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Valida, optimiza y guarda los documentos adjuntos en el almacenamiento privado.
 * Imágenes: se corrige la orientación, se reducen a 1920 px y se recomprimen en JPEG.
 * PDF: se copian tal cual tras validar la cabecera y que se puedan abrir.
 */
class AttachmentStore(private val context: Context) {

    private val rootDir = File(context.filesDir, "attachments").apply { mkdirs() }
    private val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }

    class FileTooLargeException : IOException()

    fun reportDir(reportId: String) = File(rootDir, reportId).apply { mkdirs() }

    /** Uri temporal (FileProvider) donde la app de cámara escribirá la foto. */
    fun newCameraUri(): Uri {
        cameraDir.mkdirs()
        val file = File(cameraDir, "captura_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }

    fun deleteCameraTemp(uri: Uri) {
        uri.lastPathSegment?.let { File(cameraDir, it).delete() }
    }

    fun import(uri: Uri, reportId: String, source: AttachmentSource): ImportResult {
        val resolver = context.contentResolver
        val displayName = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull()

        val mime = normalizeMime(resolver.getType(uri)) ?: mimeFromName(displayName)
        if (mime !in ALLOWED) {
            return ImportResult.Rejected("Formato no permitido (${mime ?: "desconocido"}). Usa JPG, PNG o PDF")
        }

        val (hash, size) = try {
            hashAndSize(uri)
        } catch (e: FileTooLargeException) {
            return ImportResult.Rejected("El archivo supera el máximo de 10 MB por adjunto")
        } catch (e: Exception) {
            return ImportResult.Rejected("No fue posible leer el archivo seleccionado")
        }
        if (size == 0L) return ImportResult.Rejected("El archivo está vacío")
        if (size > MAX_BYTES) return ImportResult.Rejected("El archivo supera el máximo de 10 MB por adjunto")

        val name = when {
            mime == "application/pdf" -> displayName ?: "documento.pdf"
            source == AttachmentSource.CAMARA ->
                "Foto_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date()) + ".jpg"
            else -> (displayName?.substringBeforeLast('.') ?: "imagen") + ".jpg"
        }

        return try {
            if (mime == "application/pdf") importPdf(uri, reportId, name, hash, size, source)
            else importImage(uri, reportId, name, hash, size, source)
        } catch (e: Exception) {
            ImportResult.Rejected("No fue posible leer el archivo seleccionado")
        }
    }

    private fun importImage(
        uri: Uri, reportId: String, name: String, hash: String, originalSize: Long, source: AttachmentSource,
    ): ImportResult {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return ImportResult.Rejected("La imagen está dañada o no se puede leer")
        }

        val opts = BitmapFactory.Options().apply {
            inSampleSize = BitmapUtils.sampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return ImportResult.Rejected("La imagen está dañada o no se puede leer")

        val rotation = runCatching {
            resolver.openInputStream(uri)?.use { exifRotation(ExifInterface(it)) }
        }.getOrNull() ?: 0

        val scale = minOf(1f, MAX_DIMENSION.toFloat() / maxOf(decoded.width, decoded.height))
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(rotation.toFloat())
        }
        var bitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (bitmap.hasAlpha()) {
            // Las transparencias de un PNG se aplanan sobre blanco antes de pasar a JPEG
            val flat = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            Canvas(flat).apply {
                drawColor(Color.WHITE)
                drawBitmap(bitmap, 0f, 0f, null)
            }
            bitmap = flat
        }
        val warning = qualityWarning(bitmap)

        val file = File(reportDir(reportId), UUID.randomUUID().toString() + ".jpg")
        return try {
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            ImportResult.Ok(
                Attachment(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    mimeType = "image/jpeg",
                    sizeBytes = file.length(),
                    originalSizeBytes = originalSize,
                    path = file.absolutePath,
                    sha256 = hash,
                    source = source,
                    width = bitmap.width,
                    height = bitmap.height,
                ),
                warning
            )
        } catch (e: IOException) {
            file.delete()
            ImportResult.Rejected("No hay espacio suficiente para guardar el documento")
        }
    }

    private fun importPdf(
        uri: Uri, reportId: String, name: String, hash: String, size: Long, source: AttachmentSource,
    ): ImportResult {
        val file = File(reportDir(reportId), UUID.randomUUID().toString() + ".pdf")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { input.copyTo(it) }
            } ?: return ImportResult.Rejected("No fue posible leer el PDF")
        } catch (e: IOException) {
            file.delete()
            return ImportResult.Rejected("No hay espacio suficiente para guardar el documento")
        }

        val header = file.inputStream().use { input -> ByteArray(5).also { input.read(it) } }
        if (String(header, Charsets.US_ASCII) != "%PDF-") {
            file.delete()
            return ImportResult.Rejected("El archivo no es un PDF válido")
        }
        val pages = try {
            PdfUtils.pageCount(file)
        } catch (e: Exception) {
            file.delete()
            return ImportResult.Rejected("El PDF está dañado o protegido con contraseña")
        }
        return ImportResult.Ok(
            Attachment(
                id = UUID.randomUUID().toString(),
                name = name,
                mimeType = "application/pdf",
                sizeBytes = file.length(),
                originalSizeBytes = size,
                path = file.absolutePath,
                sha256 = hash,
                source = source,
                pages = pages,
            )
        )
    }

    /** Avisos de calidad: resolución baja o imagen demasiado oscura. */
    private fun qualityWarning(bitmap: Bitmap): String? {
        if (minOf(bitmap.width, bitmap.height) < 600) {
            return "Resolución baja: verifica que el documento sea legible"
        }
        val small = Bitmap.createScaledBitmap(bitmap, 24, 24, true)
        var sum = 0.0
        for (x in 0 until 24) for (y in 0 until 24) {
            val p = small.getPixel(x, y)
            sum += 0.299 * Color.red(p) + 0.587 * Color.green(p) + 0.114 * Color.blue(p)
        }
        return if (sum / 576 < 55) "La imagen se ve muy oscura: considera repetir la captura" else null
    }

    private fun exifRotation(exif: ExifInterface): Int =
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun hashAndSize(uri: Uri): Pair<String, Long> {
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        val input = context.contentResolver.openInputStream(uri) ?: throw IOException("Sin acceso al archivo")
        input.use {
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = it.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_BYTES) throw FileTooLargeException()
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) } to total
    }

    private fun normalizeMime(mime: String?): String? = when (val m = mime?.lowercase(Locale.ROOT)) {
        null -> null
        "image/jpg", "image/pjpeg" -> "image/jpeg"
        else -> m
    }

    private fun mimeFromName(name: String?): String? =
        when (name?.substringAfterLast('.', "")?.lowercase(Locale.ROOT)) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> null
        }

    /** Elimina del disco los archivos del reporte que ya no están en [keep]. */
    fun prune(reportId: String, keep: List<Attachment>) {
        val keepPaths = keep.mapNotNull { it.path }.toSet()
        File(rootDir, reportId).listFiles()?.forEach { if (it.absolutePath !in keepPaths) it.delete() }
    }

    /** Borra carpetas de reportes que ya no existen (ni en la lista ni como borrador). */
    fun pruneOrphans(referenced: Set<String>) {
        rootDir.listFiles()?.forEach { if (it.name !in referenced) it.deleteRecursively() }
    }

    fun deleteReportFiles(reportId: String) {
        File(rootDir, reportId).deleteRecursively()
    }

    fun deleteAll() {
        rootDir.deleteRecursively()
        rootDir.mkdirs()
        cameraDir.deleteRecursively()
    }

    companion object {
        const val MAX_BYTES = 10L * 1024 * 1024
        const val MAX_DIMENSION = 1920
        const val JPEG_QUALITY = 82
        val ALLOWED = setOf("image/jpeg", "image/png", "application/pdf")
    }
}

package com.example.vitairosapp.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.vitairosapp.data.AttachmentSource
import com.example.vitairosapp.data.Catalogs
import com.example.vitairosapp.data.ReportStatus
import com.example.vitairosapp.ui.AppViewModel
import com.example.vitairosapp.ui.components.ActionRow
import com.example.vitairosapp.ui.components.AttachmentPreviewDialog
import com.example.vitairosapp.ui.components.AttachmentRow
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.Divider
import com.example.vitairosapp.ui.components.EmptyState
import com.example.vitairosapp.ui.components.FieldLabel
import com.example.vitairosapp.ui.components.Hint
import com.example.vitairosapp.ui.components.InfoLine
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.NoticeKind
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.SectionTitle
import com.example.vitairosapp.ui.components.SelectField
import com.example.vitairosapp.ui.components.StatusPill
import com.example.vitairosapp.ui.components.StepIndicator
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.components.vitairosFieldColors
import com.example.vitairosapp.ui.formatDate
import com.example.vitairosapp.ui.theme.Bone
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted

private const val NO_APLICA = "No aplica"
private const val MAX_NOTES = 600

enum class PendingPick { IMAGE, PDF }

@Composable
private fun MissingDraft(vm: AppViewModel) {
    ScreenColumn {
        EmptyState("＋", "No hay un reporte en curso.")
        VButton("Crear nuevo reporte", vm::startNewReport, Modifier.fillMaxWidth())
    }
}

// ---------------- Paso 1: formulario ----------------

@Composable
fun ReportFormScreen(vm: AppViewModel) {
    val draft = vm.draft ?: return MissingDraft(vm)
    ScreenColumn {
        StepIndicator(0)
        if (vm.isEditing) {
            Notice("Editando un reporte guardado. El ID, la fecha de creación y la trazabilidad no se modifican.")
        } else {
            Notice("Catálogos provisionales para demostración. VITAIROS entregará o validará los definitivos.")
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Tipo de reporte *")
            SelectField(
                value = draft.type,
                options = Catalogs.TIPOS,
                placeholder = "Seleccionar tipo",
                onSelect = { v -> vm.updateDraft { it.copy(type = v) } }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Proceso asociado *")
            SelectField(
                value = draft.process,
                options = Catalogs.PROCESOS,
                placeholder = "Seleccionar proceso",
                onSelect = { v -> vm.updateDraft { it.copy(process = v) } }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Área / faena")
                OutlinedTextField(
                    value = draft.area,
                    onValueChange = { v -> vm.updateDraft { it.copy(area = v.take(80)) } },
                    placeholder = { Text("Si corresponde") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = vitairosFieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Turno")
                SelectField(
                    value = draft.shift.ifBlank { NO_APLICA },
                    options = listOf(NO_APLICA) + Catalogs.TURNOS,
                    placeholder = NO_APLICA,
                    onSelect = { v -> vm.updateDraft { it.copy(shift = if (v == NO_APLICA) "" else v) } }
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Observaciones")
            OutlinedTextField(
                value = draft.notes,
                onValueChange = { v -> vm.updateDraft { it.copy(notes = v.take(MAX_NOTES)) } },
                placeholder = { Text("Descripción breve del reporte") },
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                colors = vitairosFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            )
            Hint("${draft.notes.length}/$MAX_NOTES")
        }

        VCard {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("ID automático: ") }
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(draft.id) }
                },
                color = Ink, fontSize = 13.sp
            )
            Text("Usuario: ${draft.user} · ${formatDate(draft.createdAt)}", color = Muted, fontSize = 12.5.sp)
        }

        VButton("Continuar a adjuntos →", vm::goToAttachments, Modifier.fillMaxWidth())
        VButton(
            "Guardar borrador",
            { vm.saveDraft(ReportStatus.BORRADOR) },
            Modifier.fillMaxWidth(),
            ButtonStyle.Secondary
        )
    }
}

// ---------------- Paso 2: adjuntos ----------------

private fun filePermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= 34 -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
    Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private fun Context.hasPermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Mensaje del diálogo de permiso denegado y si conviene abrir los Ajustes del sistema. */
private data class PermissionProblem(val message: String, val permanent: Boolean)

@Composable
fun AttachmentsScreen(vm: AppViewModel) {
    val draft = vm.draft ?: return MissingDraft(vm)
    val context = LocalContext.current
    var preview by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingPick by rememberSaveable { mutableStateOf<PendingPick?>(null) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var problem by remember { mutableStateOf<PermissionProblem?>(null) }
    var cameraGranted by remember { mutableStateOf(context.hasPermission(Manifest.permission.CAMERA)) }
    var filesGranted by remember { mutableStateOf(filePermissions().any { context.hasPermission(it) }) }

    fun onDenied(permissionName: String, permission: String) {
        val activity = context.findActivity()
        val permanent = activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        problem = if (permanent) {
            PermissionProblem(
                "El permiso de $permissionName fue denegado. Para adjuntar documentos debes habilitarlo en Ajustes del sistema.",
                true
            )
        } else {
            PermissionProblem(
                "Se necesita el permiso de $permissionName para adjuntar documentos. Puedes volver a intentarlo cuando quieras.",
                false
            )
        }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        cameraUri?.let { vm.onCameraResult(it, success) }
        cameraUri = null
    }
    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        vm.importAttachments(uris.take(vm.remainingSlots()), AttachmentSource.GALERIA)
    }
    val pickPdf = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.importAttachments(listOf(it), AttachmentSource.PDF) }
    }

    fun launchCamera() {
        val uri = vm.newCameraUri()
        cameraUri = uri
        try {
            takePicture.launch(uri)
        } catch (e: ActivityNotFoundException) {
            cameraUri = null
            problem = PermissionProblem("No se encontró una aplicación de cámara en este dispositivo.", false)
        }
    }

    fun launchPicker(pick: PendingPick) {
        try {
            if (pick == PendingPick.PDF) pickPdf.launch("application/pdf") else pickImages.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            problem = PermissionProblem(
                "No se encontró una aplicación para seleccionar archivos en este dispositivo.", false
            )
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
        if (granted) launchCamera() else onDenied("cámara", Manifest.permission.CAMERA)
    }
    val filesPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // Con acceso parcial (fotos seleccionadas) también se puede continuar
        val granted = result.values.any { it }
        filesGranted = granted
        val pick = pendingPick
        pendingPick = null
        if (granted && pick != null) launchPicker(pick)
        else if (!granted) onDenied("acceso a fotos y archivos", filePermissions().first())
    }

    fun onCamera() {
        if (!vm.canAddAttachment()) return
        if (context.hasPermission(Manifest.permission.CAMERA)) launchCamera()
        else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    fun onPick(pick: PendingPick) {
        if (!vm.canAddAttachment()) return
        if (filePermissions().any { context.hasPermission(it) }) {
            launchPicker(pick)
        } else {
            pendingPick = pick
            filesPermission.launch(filePermissions())
        }
    }

    LaunchedEffect(Unit) {
        cameraGranted = context.hasPermission(Manifest.permission.CAMERA)
        filesGranted = filePermissions().any { context.hasPermission(it) }
    }

    ScreenColumn {
        StepIndicator(1)
        Notice(
            "Se admiten imágenes JPG/PNG y PDF, hasta 5 adjuntos de máximo 10 MB cada uno. " +
                "Las fotos se optimizan y todo queda en el almacenamiento privado de la app."
        )
        ActionRow {
            VButton("📷 Cámara", ::onCamera, Modifier.weight(1f), ButtonStyle.Secondary)
            VButton("▧ Galería", { onPick(PendingPick.IMAGE) }, Modifier.weight(1f), ButtonStyle.Secondary)
        }
        VButton(
            "📄 Seleccionar PDF", { onPick(PendingPick.PDF) },
            Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            ButtonStyle.Ghost
        )
        Hint(
            "Permisos del dispositivo: Cámara " + (if (cameraGranted) "✓ concedido" else "— pendiente") +
                " · Archivos " + (if (filesGranted) "✓ concedido" else "— pendiente")
        )
        if (vm.importing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = Copper, strokeWidth = 2.dp)
                Spacer(Modifier.size(10.dp))
                Hint("Validando y optimizando documento…")
            }
        }

        SectionTitle("Documentos adjuntos (${draft.attachments.size}/5)", Modifier.padding(top = 6.dp))
        if (draft.attachments.isEmpty()) {
            EmptyState("▧", "Sin adjuntos. Agrega al menos uno.")
        } else {
            draft.attachments.forEach { a ->
                AttachmentRow(a, onView = { preview = a.id }, onRemove = { vm.removeAttachment(a) })
            }
        }

        ActionRow {
            VButton("← Volver", vm::backToForm, Modifier.weight(1f), ButtonStyle.Secondary)
            VButton("Revisar →", vm::goToReview, Modifier.weight(1f))
        }
    }

    draft.attachments.firstOrNull { it.id == preview }?.let {
        AttachmentPreviewDialog(it) { preview = null }
    }

    problem?.let { p ->
        AlertDialog(
            onDismissRequest = { problem = null },
            containerColor = Bone,
            title = { Text("Permiso requerido", color = Ink) },
            text = { Text(p.message, color = Muted) },
            confirmButton = {
                if (p.permanent) {
                    TextButton(onClick = {
                        problem = null
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", context.packageName, null))
                        )
                    }) { Text("Abrir ajustes", color = Ink, fontWeight = FontWeight.Bold) }
                } else {
                    TextButton(onClick = { problem = null }) {
                        Text("Entendido", color = Ink, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = if (p.permanent) {
                { TextButton(onClick = { problem = null }) { Text("Cancelar", color = Ink) } }
            } else null
        )
    }
}

// ---------------- Paso 3: revisión ----------------

@Composable
fun ReviewScreen(vm: AppViewModel) {
    val draft = vm.draft ?: return MissingDraft(vm)
    var preview by rememberSaveable { mutableStateOf<String?>(null) }
    ScreenColumn {
        StepIndicator(2)
        VCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Resumen del reporte", Modifier.weight(1f), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                StatusPill(draft.status)
            }
            Divider()
            InfoLine("Tipo", draft.type)
            InfoLine("Proceso", draft.process)
            InfoLine("Área", draft.area.ifBlank { NO_APLICA })
            InfoLine("Turno", draft.shift.ifBlank { NO_APLICA })
            InfoLine("Usuario", draft.user)
            InfoLine("Creación", formatDate(draft.createdAt))
            InfoLine("Observaciones", draft.notes.ifBlank { "Sin observaciones" })
            InfoLine("Adjuntos", "${draft.attachments.size}")
        }

        draft.attachments.forEach { a -> AttachmentRow(a, onView = { preview = a.id }) }

        Row(
            Modifier.clickable { vm.qualityConfirmed = !vm.qualityConfirmed },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = vm.qualityConfirmed,
                onCheckedChange = { vm.qualityConfirmed = it },
                colors = CheckboxDefaults.colors(checkedColor = Copper, uncheckedColor = Muted)
            )
            Text(
                "Confirmo que los documentos corresponden al reporte y son legibles.",
                color = Ink, fontSize = 15.sp, lineHeight = 22.sp
            )
        }

        if (!vm.online) {
            Notice(
                "Sin conexión: al enviar, el reporte quedará guardado y pendiente en la cola. " +
                    "Se enviará automáticamente al recuperar la conexión.",
                NoticeKind.Danger
            )
        }

        ActionRow {
            VButton("← Corregir", vm::backToAttachments, Modifier.weight(1f), ButtonStyle.Secondary)
            VButton(
                if (vm.online) "Enviar ahora" else "Guardar pendiente",
                {
                    if (vm.requireQualityConfirmation()) vm.saveDraft(ReportStatus.EN_COLA, sendNow = true)
                },
                Modifier.weight(1f),
                ButtonStyle.Copper
            )
        }
        VButton(
            "Guardar en cola (enviar después)",
            { if (vm.requireQualityConfirmation()) vm.saveDraft(ReportStatus.EN_COLA) },
            Modifier.fillMaxWidth(),
            ButtonStyle.Secondary
        )
        VButton(
            "Guardar como borrador",
            { vm.saveDraft(ReportStatus.BORRADOR) },
            Modifier.fillMaxWidth(),
            ButtonStyle.Ghost
        )
        Spacer(Modifier.height(4.dp))
    }

    draft.attachments.firstOrNull { it.id == preview }?.let {
        AttachmentPreviewDialog(it) { preview = null }
    }
}


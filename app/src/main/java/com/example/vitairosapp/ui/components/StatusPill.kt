package com.example.vitairosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.DangerBg
import com.example.vitairosapp.ui.theme.Deep
import com.example.vitairosapp.ui.theme.InfoNoticeBg
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.SuccessBg
import com.example.vitairosapp.ui.theme.SuccessText
import com.example.vitairosapp.ui.theme.WarnBg
import com.example.vitairosapp.ui.theme.WarnText

@Composable
fun StatusPill(status: ReportStatus) {
    val symbol = when (status) {
        ReportStatus.BORRADOR -> "◌"
        ReportStatus.ENVIANDO -> "↻"
        ReportStatus.EN_COLA -> "◷"
        ReportStatus.SINCRONIZADO -> "✓"
        ReportStatus.ERROR -> "!"
    }
    val (bg, fg) = when (status) {
        ReportStatus.BORRADOR -> Mist to Deep
        ReportStatus.EN_COLA -> WarnBg to WarnText
        ReportStatus.ENVIANDO -> InfoNoticeBg to CopperDark
        ReportStatus.SINCRONIZADO -> SuccessBg to SuccessText
        ReportStatus.ERROR -> DangerBg to Danger
    }
    Text(
        "$symbol ${status.label}",
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg, fontSize = 12.5.sp, fontWeight = FontWeight.Bold
    )
}

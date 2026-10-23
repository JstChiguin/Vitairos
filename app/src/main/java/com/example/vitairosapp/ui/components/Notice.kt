package com.example.vitairosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.DangerBg
import com.example.vitairosapp.ui.theme.InfoNoticeBg
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.SuccessNoticeBg
import com.example.vitairosapp.ui.theme.SuccessText

enum class NoticeKind { Info, Success, Danger }

@Composable
fun Notice(text: String, kind: NoticeKind = NoticeKind.Info) {
    val (bg, bar) = when (kind) {
        NoticeKind.Info -> InfoNoticeBg to Copper
        NoticeKind.Success -> SuccessNoticeBg to SuccessText
        NoticeKind.Danger -> DangerBg to Danger
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
    ) {
        Box(
            Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(bar)
        )
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = Ink, fontSize = 13.sp, lineHeight = 19.sp
        )
    }
}

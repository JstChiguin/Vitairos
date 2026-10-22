package com.example.vitairosapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.ui.theme.Bone
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.DangerBg
import com.example.vitairosapp.ui.theme.Deep
import com.example.vitairosapp.ui.theme.DisabledBg
import com.example.vitairosapp.ui.theme.Line
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.OnCopper

enum class ButtonStyle { Primary, Secondary, Copper, Ghost, Danger }

@Composable
fun VButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val (bg, fg) = when (style) {
        ButtonStyle.Primary -> Deep to Color.White
        ButtonStyle.Secondary -> Mist to Deep
        ButtonStyle.Copper -> Copper to OnCopper
        ButtonStyle.Ghost -> Color.White to Deep
        ButtonStyle.Danger -> DangerBg to Danger
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = if (style == ButtonStyle.Primary) DisabledBg else bg.copy(alpha = 0.5f),
            disabledContentColor = if (style == ButtonStyle.Primary) Bone else fg.copy(alpha = 0.5f),
        ),
        border = if (style == ButtonStyle.Ghost) BorderStroke(1.dp, Line) else null,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

/** Fila de botones que se reparten el ancho. */
@Composable
fun ActionRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

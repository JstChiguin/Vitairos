package com.example.vitairosapp.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun FieldLabel(text: String) {
    Text(text, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun Hint(text: String, color: Color = Muted) {
    Text(text, color = color, fontSize = 11.5.sp, lineHeight = 17.sp, letterSpacing = 0.1.sp)
}

@Composable
fun InfoLine(label: String, value: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$label:") }
            append(" ")
            append(value)
        },
        color = Ink, fontSize = 13.sp, lineHeight = 18.sp
    )
}

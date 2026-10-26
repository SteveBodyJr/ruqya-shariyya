package com.digitalbeavertz.ruqya.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalbeavertz.ruqya.ui.theme.RT

/**
 * The deep green header with a gold mosque-arch line rising to a point above its centre text
 * and a few small gold stars. Flat colours; the bottom edge dips gently in the middle.
 */
@Composable
fun ArchHeader(modifier: Modifier = Modifier, height: Dp = 250.dp, content: @Composable BoxScope.() -> Unit) {
    val c = RT.colors
    Box(modifier.fillMaxWidth().height(height)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val dip = 22.dp.toPx()
            val body = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h - dip)
                cubicTo(w * 0.70f, h - dip, w * 0.62f, h, w / 2f, h)
                cubicTo(w * 0.38f, h, w * 0.30f, h - dip, 0f, h - dip)
                close()
            }
            drawPath(body, c.header)

            // The arch: flat along the bottom, then an onion dome up to a point over the clock.
            val base = h - dip - 14.dp.toPx()
            val top = h * 0.25f
            val left = w * 0.13f
            val right = w * 0.87f
            val arch = Path().apply {
                moveTo(0f, base)
                lineTo(left, base)
                cubicTo(left - w * 0.02f, base - (base - top) * 0.75f, w / 2f - w * 0.14f, top + (base - top) * 0.18f, w / 2f, top)
                cubicTo(w / 2f + w * 0.14f, top + (base - top) * 0.18f, right + w * 0.02f, base - (base - top) * 0.75f, right, base)
                lineTo(w, base)
            }
            drawPath(arch, c.gold, style = Stroke(width = 2.dp.toPx()))
            star(Offset(w / 2f, top - 10.dp.toPx()), 6.dp.toPx(), c.gold)

            listOf(0.08f to 0.62f, 0.15f to 0.80f, 0.90f to 0.60f, 0.84f to 0.82f, 0.30f to 0.52f, 0.70f to 0.50f)
                .forEach { (x, y) -> star(Offset(w * x, h * y), 4.dp.toPx(), c.gold.copy(alpha = 0.45f)) }
        }
        content()
    }
}

/** A small four-pointed star. */
private fun DrawScope.star(center: Offset, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(center.x, center.y - r)
        quadraticTo(center.x, center.y, center.x + r, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + r)
        quadraticTo(center.x, center.y, center.x - r, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - r)
        close()
    }
    drawPath(p, color)
}

/** A number inside an eight-pointed star outline (two overlapping squares), as in the reference lists. */
@Composable
fun StarBadge(text: String, size: Dp = 36.dp, color: Color = RT.colors.green, filled: Boolean = false) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = this.size.minDimension * 0.66f
            val o = Offset((this.size.width - s) / 2f, (this.size.height - s) / 2f)
            val stroke = Stroke(width = 1.6.dp.toPx())
            listOf(0f, 45f).forEach { deg ->
                rotate(deg) {
                    if (filled) drawRect(color, o, androidx.compose.ui.geometry.Size(s, s))
                    else drawRect(color, o, androidx.compose.ui.geometry.Size(s, s), style = stroke)
                }
            }
        }
        Text(
            text, color = if (filled) Color.White else color, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = if (text.length > 2) 10.sp else 12.sp),
        )
    }
}

/** An outlined tile for the Home grid: an icon in a soft circle above a label. */
@Composable
fun OutlinedTile(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = RT.colors
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.clip(shape).background(c.card).border(1.2.dp, c.green.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick).padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(c.greenSoft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = c.green, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, color = c.ink, textAlign = TextAlign.Center)
    }
}

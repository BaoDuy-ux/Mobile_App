package com.example.agriculture_management.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Forest = Color(0xFF4F763C)
val Leaf = Color(0xFF8EB65B)
val Cream = Color(0xFFF8F9F0)
val Pale = Color(0xFFE5EEDB)
val Ink = Color(0xFF20291F)
val Muted = Color(0xFF7D8578)
val Line = Color(0xFFEBEEE5)
val Coral = Color(0xFFD97063)
val Violet = Color(0xFF8880AB)

@Composable
fun FarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Forest, onPrimary = Color.White,
            primaryContainer = Pale, onPrimaryContainer = Forest, background = Cream,
            surface = Color.White, onSurface = Ink, onBackground = Ink,
            surfaceVariant = Pale, onSurfaceVariant = Muted, outline = Line, error = Coral),
        typography = Typography(
            headlineLarge = androidx.compose.ui.text.TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp),
            headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
            titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold),
            titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
            bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, lineHeight = 20.sp),
            labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)),
        content = content
    )
}

@Composable
fun Page(title: String, back: () -> Unit, action: (@Composable () -> Unit)? = null,
         content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Quay lại", Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            action?.invoke()
        }
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(22.dp))
        content()
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, pale: Boolean = false, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 50.dp),
        shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(
            containerColor = if (pale) Pale else Forest, contentColor = if (pale) Forest else Color.White)) {
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FarmCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color.White).padding(16.dp), content = content)
}

@Composable
fun Section(title: String, trailing: String? = null, onClick: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (trailing != null) TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) { Text(trailing, fontSize = 12.sp) }
    }
}

@Composable
fun Badge(text: String, color: Color = Forest) {
    Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(color.copy(alpha = .10f)).padding(horizontal = 9.dp, vertical = 4.dp))
}

@Composable
fun IconTile(icon: ImageVector, color: Color = Forest, size: Int = 46) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size((size * .53f).dp), tint = color)
    }
}

@Composable
fun MenuRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
        Icon(icon, null, Modifier.size(21.dp), tint = Forest)
        Text(title, Modifier.weight(1f), fontSize = 14.sp)
        if (subtitle != null) Text(subtitle, color = Muted, fontSize = 11.sp)
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(18.dp), tint = Muted)
    }
}

@Composable
fun Tabs(items: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        items.forEachIndexed { index, text ->
            Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (index == selected) Forest else Color.Transparent)
                .clickable { onSelect(index) }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                Text(text, fontSize = 12.sp, color = if (index == selected) Color.White else Muted, maxLines = 1)
            }
        }
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, placeholder: String = "", lines: Int = 1,
          keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
          trailing: (@Composable () -> Unit)? = null) {
    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth().padding(bottom = 17.dp),
        placeholder = { Text(placeholder, color = Muted, fontSize = 13.sp) }, shape = RoundedCornerShape(12.dp),
        singleLine = lines == 1, minLines = lines, trailingIcon = trailing,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Line, focusedBorderColor = Forest,
            unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
}

/** Vector artwork stays sharp in previews and works offline at every screen density. */
@Composable
fun LeafMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        fun leaf(sx: Float, sy: Float, cx: Float, cy: Float, ex: Float, ey: Float, color: Color) {
            drawPath(Path().apply { moveTo(sx*w, sy*h); quadraticBezierTo(cx*w, cy*h, ex*w, ey*h)
                quadraticBezierTo(sx*w, ey*h, sx*w, sy*h); close() }, color)
        }
        leaf(.48f,.91f,.02f,.8f,.17f,.37f,Leaf)
        leaf(.5f,.9f,.95f,.8f,.88f,.20f,Forest)
        leaf(.49f,.61f,.22f,.26f,.39f,.03f,Color(0xFFB1CC72))
        drawLine(Cream, Offset(.48f*w,.93f*h), Offset(.77f*w,.39f*h), 2.dp.toPx())
        drawLine(Cream, Offset(.48f*w,.91f*h), Offset(.25f*w,.51f*h), 2.dp.toPx())
    }
}

@Composable
fun FarmArt(modifier: Modifier = Modifier, farmer: Boolean = false) {
    Canvas(modifier.clip(RoundedCornerShape(18.dp))) {
        val w = size.width; val h = size.height
        drawRect(Color(0xFFE8F0E2))
        drawCircle(Color(0xFFF9F8D8), w*.105f, Offset(w*.76f,h*.18f))
        fun hill(color: Color, y: Float, rise: Float) {
            drawPath(Path().apply { moveTo(0f,h*y); cubicTo(w*.23f,h*(y-rise),w*.29f,h*(y+rise),w*.51f,h*y)
                cubicTo(w*.7f,h*(y-rise),w*.88f,h*(y-rise),w,h*y); lineTo(w,h); lineTo(0f,h); close() },color)
        }
        hill(Color(0xFFB4C9A3),.45f,.25f); hill(Color(0xFF8CAA72),.59f,.22f)
        hill(Color(0xFFC7D792),.69f,.21f); hill(Color(0xFF92B460),.82f,.2f)
        for (i in 0..7) {
            val x=w*(i/7f)
            drawPath(Path().apply { moveTo(w*.58f,h*.60f); quadraticBezierTo(x,h*.76f,x-w*.45f,h*1.15f) },
                if(i%2==0) Color(0xFFDBDEA0) else Forest.copy(alpha=.5f), style=Stroke(w*.045f))
        }
        // Farmhouse, windbreak trees and foreground crops.
        drawRect(Color(0xFFF7EDC9),Offset(w*.23f,h*.48f),Size(w*.15f,h*.13f))
        drawPath(Path().apply { moveTo(w*.20f,h*.49f); lineTo(w*.30f,h*.39f); lineTo(w*.41f,h*.49f); close() },Color(0xFF7D8259))
        drawRect(Forest,Offset(w*.29f,h*.54f),Size(w*.035f,h*.07f))
        listOf(.08f,.14f,.84f,.94f).forEach { x ->
            drawLine(Color(0xFF6C7950),Offset(w*x,h*.32f),Offset(w*x,h*.61f),w*.013f)
            drawOval(Forest.copy(alpha=.85f),Offset(w*(x-.05f),h*.21f),Size(w*.11f,h*.27f))
            drawCircle(Leaf,w*.045f,Offset(w*(x-.025f),h*.30f))
        }
        for(i in 0..15) {
            val x = w*i/15f; val y = h*(.9f + (i%3)*.025f)
            drawLine(Forest,Offset(x,h),Offset(x,y-h*.12f),2.dp.toPx())
            rotate(-35f,Offset(x,y)) { drawOval(Forest,Offset(x-w*.03f,y-h*.10f),Size(w*.045f,h*.12f)) }
            rotate(35f,Offset(x,y)) { drawOval(Leaf,Offset(x,y-h*.08f),Size(w*.04f,h*.11f)) }
        }
        if(farmer) {
            drawRect(Color(0xFFE0AE79),Offset(w*.56f,h*.43f),Size(w*.07f,h*.12f))
            drawOval(Color(0xFF365E40),Offset(w*.48f,h*.49f),Size(w*.24f,h*.38f))
            drawCircle(Color(0xFFE0AE79),w*.063f,Offset(w*.59f,h*.39f))
            drawCircle(Ink,w*.007f,Offset(w*.555f,h*.38f))
            drawPath(Path().apply { moveTo(w*.54f,h*.4f);lineTo(w*.515f,h*.43f);lineTo(w*.55f,h*.44f);close() },Color(0xFFE0AE79))
            drawPath(Path().apply { moveTo(w*.43f,h*.35f); lineTo(w*.59f,h*.20f); lineTo(w*.76f,h*.36f); close() },Color(0xFFDDC47E))
            drawLine(Color(0xFFE0AE79),Offset(w*.51f,h*.56f),Offset(w*.39f,h*.62f),w*.05f)
            rotate(-18f,Offset(w*.39f,h*.58f)) {
                drawRoundRect(Ink,Offset(w*.31f,h*.47f),Size(w*.14f,h*.18f), androidx.compose.ui.geometry.CornerRadius(5f))
                drawRect(Color(0xFFDCE8CA),Offset(w*.33f,h*.49f),Size(w*.10f,h*.13f))
            }
        }
    }
}

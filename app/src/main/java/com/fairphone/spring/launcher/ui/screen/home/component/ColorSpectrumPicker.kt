package com.fairphone.spring.launcher.ui.screen.home.component

import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairphone.spring.launcher.R

data class GradientPresetItem(
    val name: String,
    val primary: Long,
    val secondary: Long,
    val emoji: String
)

val CURATED_GRADIENT_PRESETS = listOf(
    GradientPresetItem("Kozmik Mor", 0xFF7C3AEDL, 0xFFDB2777L, "🌌"),
    GradientPresetItem("Derin Okyanus", 0xFF1E40AFL, 0xFF06B6D4L, "🌊"),
    GradientPresetItem("Günbatımı", 0xFFBE123CL, 0xFFF59E0BL, "🌅"),
    GradientPresetItem("Zümrüt Vadi", 0xFF065F46L, 0xFF10B981L, "🍃"),
    GradientPresetItem("Gece Neonu", 0xFF4C1D95L, 0xFF2563EBL, "🔮"),
    GradientPresetItem("Şeftali & Bal", 0xFFC2410CL, 0xFFFBBF24L, "🍑"),
    GradientPresetItem("Yaban Mersini", 0xFF1E1B4BL, 0xFF6366F1L, "🫐"),
    GradientPresetItem("Kiraz Çiçeği", 0xFF9D174DL, 0xFFF472B6L, "🌸"),
    GradientPresetItem("Kuzey Buzulu", 0xFF0F172AL, 0xFF38BDF8L, "❄️"),
    GradientPresetItem("Titanyum", 0xFF18181BL, 0xFF3F3F46L, "🖤")
)

private fun colorToHsv(colorLong: Long): Triple<Float, Float, Float> {
    val colorInt = colorLong.toInt()
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(colorInt, hsv)
    return Triple(hsv[0], hsv[1], hsv[2])
}

private fun hsvToColorLong(hue: Float, saturation: Float, value: Float): Long {
    val hsv = floatArrayOf(
        hue.coerceIn(0f, 360f),
        saturation.coerceIn(0f, 1f),
        value.coerceIn(0f, 1f)
    )
    val colorInt = AndroidColor.HSVToColor(hsv)
    return colorInt.toLong() and 0xFFFFFFFFL
}

@Composable
fun ColorSpectrumPicker(
    primaryColor: Long,
    secondaryColor: Long,
    gradientStyle: String,
    onPrimaryColorChange: (Long) -> Unit,
    onSecondaryColorChange: (Long) -> Unit,
    onGradientStyleChange: (String) -> Unit,
    isDark: Boolean,
    cardBg: Color,
    sheetTextColor: Color,
    subtitleTextColor: Color,
    modifier: Modifier = Modifier
) {
    // 0 = Primary Color, 1 = Secondary Color
    var activeColorTarget by remember { mutableIntStateOf(0) }

    val activeColorLong = if (activeColorTarget == 0) primaryColor else secondaryColor
    val (curHue, curSat, curVal) = remember(activeColorLong) { colorToHsv(activeColorLong) }

    var localHue by remember(activeColorLong) { mutableFloatStateOf(curHue) }
    var localSat by remember(activeColorLong) { mutableFloatStateOf(curSat) }
    var localVal by remember(activeColorLong) { mutableFloatStateOf(curVal) }

    val updateActiveColor = { h: Float, s: Float, v: Float ->
        localHue = h
        localSat = s
        localVal = v
        val newColor = hsvToColorLong(h, s, v)
        if (activeColorTarget == 0) {
            onPrimaryColorChange(newColor)
        } else {
            onSecondaryColorChange(newColor)
        }
    }

    val rainbowColors = remember {
        listOf(
            Color(0xFFFF0000),
            Color(0xFFFF7F00),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFF8B00FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000)
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E323D) else Color(0xFFE2E4E9)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.wallpaper_color_scale_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = sheetTextColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Canlı Skala",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.wallpaper_color_scale_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = subtitleTextColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mini Realtime Gradient Preview Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        when (gradientStyle) {
                            "radial" -> Modifier.background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(secondaryColor),
                                        Color(primaryColor),
                                        Color(0xFF090A10)
                                    ),
                                    radius = 350f
                                )
                            )
                            "diagonal" -> Modifier.background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(primaryColor),
                                        Color(secondaryColor),
                                        Color(0xFF0D0E17)
                                    ),
                                    start = Offset.Zero,
                                    end = Offset.Infinite
                                )
                            )
                            else -> Modifier.background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(primaryColor),
                                        Color(secondaryColor),
                                        Color(0xFF0C0D15)
                                    )
                                )
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Önizleme • ${if (gradientStyle == "radial") "Radyal" else if (gradientStyle == "diagonal") "Çapraz" else "Dikey"}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gradient Style Selection Chips
            Text(
                text = "Degrade Akış Tarzı",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = sheetTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val styles = listOf(
                    "linear" to ("↕️ " + stringResource(R.string.color_style_linear)),
                    "diagonal" to ("↗️ " + stringResource(R.string.color_style_diagonal)),
                    "radial" to ("💫 " + stringResource(R.string.color_style_radial)),
                    "dynamic" to ("🫧 " + stringResource(R.string.color_style_dynamic))
                )
                styles.forEach { (key, label) ->
                    val isSelected = gradientStyle == key
                    Surface(
                        onClick = { onGradientStyleChange(key) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else if (isDark) Color(0xFF1E222B) else Color(0xFFEFF1F5),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else if (isDark) Color(0xFF2C303B) else Color(0xFFE2E4E9)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else sheetTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Selector Tabs (Primary vs Secondary Color)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary Color Card
                ColorTargetCard(
                    title = stringResource(R.string.color_primary_label),
                    subtitle = "Üst / Başlangıç",
                    color = Color(primaryColor),
                    isSelected = activeColorTarget == 0,
                    onClick = { activeColorTarget = 0 },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                // Secondary Color Card
                ColorTargetCard(
                    title = stringResource(R.string.color_secondary_label),
                    subtitle = "Alt / Bitiş",
                    color = Color(secondaryColor),
                    isSelected = activeColorTarget == 1,
                    onClick = { activeColorTarget = 1 },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Rainbow Hue Spectrum Bar & Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(activeColorLong))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.color_spectrum_hue),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = sheetTextColor
                    )
                }

                Text(
                    text = "${localHue.toInt()}°",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = subtitleTextColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rainbow Track Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Brush.horizontalGradient(rainbowColors))
                    .border(
                        0.5.dp,
                        if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f),
                        RoundedCornerShape(7.dp)
                    )
            )

            Slider(
                value = localHue,
                onValueChange = { newHue ->
                    updateActiveColor(newHue, localSat, localVal)
                },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(activeColorLong),
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Lightness / Parlaklık Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.color_spectrum_lightness),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = sheetTextColor
                )
                Text(
                    text = "${(localVal * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = subtitleTextColor
                )
            }

            Slider(
                value = localVal,
                onValueChange = { newVal ->
                    updateActiveColor(localHue, localSat, newVal)
                },
                valueRange = 0.15f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Saturation / Doygunluk Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.color_spectrum_saturation),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = sheetTextColor
                )
                Text(
                    text = "${(localSat * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = subtitleTextColor
                )
            }

            Slider(
                value = localSat,
                onValueChange = { newSat ->
                    updateActiveColor(localHue, newSat, localVal)
                },
                valueRange = 0.15f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Mood Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val moods = listOf(
                    "⚡ Canlı" to Pair(0.95f, 0.95f),
                    "🌙 Gece" to Pair(0.85f, 0.38f),
                    "🌸 Pastel" to Pair(0.40f, 0.92f),
                    "💎 Saf" to Pair(1.0f, 0.85f)
                )
                moods.forEach { (label, pair) ->
                    Surface(
                        onClick = { updateActiveColor(localHue, pair.first, pair.second) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF1E222B) else Color(0xFFEFF1F5),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2C303B) else Color(0xFFE2E4E9)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = sheetTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Curated Gradient Presets (Popüler Renk Skalası Şablonları)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hazır Renk İkilileri",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = sheetTextColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CURATED_GRADIENT_PRESETS.forEach { preset ->
                    val isPresetActive = primaryColor == preset.primary && secondaryColor == preset.secondary
                    Surface(
                        onClick = {
                            onPrimaryColorChange(preset.primary)
                            onSecondaryColorChange(preset.secondary)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPresetActive) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else if (isDark) {
                            Color(0xFF1E222B)
                        } else {
                            Color(0xFFF3F4F7)
                        },
                        border = BorderStroke(
                            if (isPresetActive) 1.5.dp else 1.dp,
                            if (isPresetActive) MaterialTheme.colorScheme.primary else if (isDark) Color(0xFF2C303B) else Color(0xFFE2E4E9)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            // Mini two-color dot
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(preset.primary), Color(preset.secondary))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${preset.emoji} ${preset.name}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isPresetActive) MaterialTheme.colorScheme.primary else sheetTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorTargetCard(
    title: String,
    subtitle: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else if (isDark) Color(0xFF2C303B) else Color(0xFFE2E4E9)
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else if (isDark) {
        Color(0xFF181B22)
    } else {
        Color(0xFFF9FAFB)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFFA0A5B5) else Color(0xFF6B7280)
                )
            }
        }
    }
}

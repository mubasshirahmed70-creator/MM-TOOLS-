package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.NavyBackground

/**
 * Ultra-Modern Top Toolbar
 * Each tool has a curated semantic accent color, frosted squircle tile, and tactile micro-border.
 */
@Composable
fun CompactToolbar(
    isDesktopMode: Boolean,
    onToggleDesktopMode: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onCopyUidClick: () -> Unit,
    onCopyCookieClick: () -> Unit,
    onTwoFactorClick: () -> Unit,
    onClearDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xF80B1220), // Rich midnight glass
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // High-Tech Frosted Tool Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF131D31),
                            Color(0xFF0E1626)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x3300E5FF),
                                Color(0x662979FF),
                                Color(0x3300E5FF)
                            )
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Mobile / Desktop Toggle (Electric Cyan)
                ModernToolbarButton(
                    icon = if (isDesktopMode) Icons.Default.DesktopWindows else Icons.Default.PhoneAndroid,
                    label = if (isDesktopMode) "Desk" else "Mobile",
                    accentColor = CyanAccent,
                    isActive = isDesktopMode,
                    testTag = "toolbar_desktop_mode",
                    onClick = onToggleDesktopMode
                )

                // 2. Create Account (Emerald / Mint)
                ModernToolbarButton(
                    icon = Icons.Default.PersonAdd,
                    label = "Create",
                    accentColor = Color(0xFF10B981),
                    isActive = false,
                    testTag = "toolbar_create_account",
                    onClick = onCreateAccountClick
                )

                // 3. Copy UID (Sky Blue)
                ModernToolbarButton(
                    icon = Icons.Default.Fingerprint,
                    label = "UID",
                    accentColor = Color(0xFF38BDF8),
                    isActive = false,
                    testTag = "toolbar_copy_uid",
                    onClick = onCopyUidClick
                )

                // 4. Copy Cookie (Golden Amber)
                ModernToolbarButton(
                    icon = Icons.Default.Cookie,
                    label = "Cookie",
                    accentColor = Color(0xFFFBBF24),
                    isActive = false,
                    testTag = "toolbar_copy_cookie",
                    onClick = onCopyCookieClick
                )

                // 5. Two-Factor Authentication (Indigo / Violet)
                ModernToolbarButton(
                    icon = Icons.Default.Security,
                    label = "2FA",
                    accentColor = Color(0xFF818CF8),
                    isActive = false,
                    testTag = "toolbar_two_factor",
                    onClick = onTwoFactorClick
                )

                // 6. Clear Data (Rose / Red)
                ModernToolbarButton(
                    icon = Icons.Default.CleaningServices,
                    label = "Clear",
                    accentColor = Color(0xFFF87171),
                    isActive = false,
                    testTag = "toolbar_clear_data",
                    onClick = onClearDataClick
                )
            }
        }
    }
}

@Composable
private fun ModernToolbarButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBorderColor by animateColorAsState(
        targetValue = if (isActive) CyanAccent else accentColor.copy(alpha = 0.25f),
        label = "activeBorder"
    )
    val tileBgColor by animateColorAsState(
        targetValue = if (isActive) CyanContainer.copy(alpha = 0.85f) else Color(0x15FFFFFF),
        label = "tileBg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tileBgColor)
            .border(
                width = if (isActive) 1.2.dp else 0.8.dp,
                color = activeBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .defaultMinSize(minWidth = 50.dp, minHeight = 48.dp)
            .testTag(testTag)
    ) {
        // Icon badge with glowing translucent backdrop
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = if (isActive) 0.3f else 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) CyanAccent else accentColor,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = if (isActive) CyanAccent else Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.2.sp,
            maxLines = 1
        )
    }
}

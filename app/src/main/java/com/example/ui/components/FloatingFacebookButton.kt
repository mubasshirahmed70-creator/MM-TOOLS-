package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextPrimary

/**
 * Modern Compact Floating Action Buttons
 * - Features the official Meta Facebook logo for the home/reload action.
 * - Upper Tools button expands options VERTICALLY upwards with sleek frosted chips.
 */
@Composable
fun FloatingActionButtonsGroup(
    onFacebookClick: () -> Unit,
    onNameClick: () -> Unit,
    onKeyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isMenuOpen) 135f else 0f,
        animationSpec = spring(),
        label = "tools_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // Quick Options Popup: Pops out VERTICALLY above the tools button
        AnimatedVisibility(
            visible = isMenuOpen,
            enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + scaleOut(targetScale = 0.8f) + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                // Option 1: USA Name Chip
                FloatingQuickChip(
                    icon = Icons.Default.Person,
                    label = "USA Name",
                    accentColor = CyanAccent,
                    testTag = "quick_option_name",
                    onClick = {
                        isMenuOpen = false
                        onNameClick()
                    }
                )

                // Option 2: 2FA Key Chip
                FloatingQuickChip(
                    icon = Icons.Default.Key,
                    label = "2FA Key",
                    accentColor = Color(0xFFFFB300),
                    testTag = "quick_option_key",
                    onClick = {
                        isMenuOpen = false
                        onKeyClick()
                    }
                )
            }
        }

        // Upper Floating Tools Button (Squircle with rotating glowing star)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isMenuOpen) CyanAccent else NavySurface.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(CyanAccent, BluePrimary.copy(alpha = 0.6f))
                    ),
                    RoundedCornerShape(14.dp)
                )
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = CyanAccent)
                .clickable { isMenuOpen = !isMenuOpen }
                .testTag("floating_utility_button")
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Quick Tools",
                    tint = if (isMenuOpen) NavyBackground else CyanAccent,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }
        }

        // Bottom Floating Button: Official Facebook Logo
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 12.dp,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .shadow(12.dp, CircleShape, spotColor = Color(0x990866FF))
                .clickable {
                    isMenuOpen = false
                    onFacebookClick()
                }
                .testTag("floating_facebook_button")
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                CyanAccent.copy(alpha = 0.8f),
                                Color(0xFF0866FF),
                                CyanAccent.copy(alpha = 0.8f)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_facebook_official),
                    contentDescription = "Facebook Home",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingQuickChip(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavySurface.copy(alpha = 0.96f),
        shadowElevation = 10.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.2.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = accentColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

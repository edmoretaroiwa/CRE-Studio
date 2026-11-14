package com.cre.studio.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors extracted from your CSS
val WellColor = Color(0xFF0B0C0E)
val ButtonTopColor = Color(0xFF323640)
val ButtonBottomColor = Color(0xFF1E2127)
val ButtonThicknessColor = Color(0xFF111317)
val LedOffColor = Color(0xFF121316)
val PlayLedActive = Color(0xFF00E676)
val RecordLedActive = Color(0xFFFF2A55)

@Composable
fun HardwareButton(
    label: String,
    isActive: Boolean = false,
    activeColor: Color = PlayLedActive,
    onClick: () -> Unit
) {
    // 1. Track the physical press state of the button
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 2. Animate the physical downward press (CSS: transform: translateY(2px))
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        label = "button_press"
    )

    // 3. Chassis Well (The dark inset background)
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(42.dp)
            .background(color = WellColor, shape = RoundedCornerShape(8.dp))
            .padding(2.dp), // Padding from CSS
        contentAlignment = Alignment.Center
    ) {
        // The actual Button Body
        Box(
            modifier = Modifier
                .offset(y = pressOffset) // Moves down when pressed
                .width(42.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(6.dp))
                // Simulate the physical thickness (CSS: 0 2px 0 #111317)
                .shadow(
                    elevation = if (isPressed) 1.dp else 4.dp,
                    shape = RoundedCornerShape(6.dp),
                    spotColor = ButtonThicknessColor,
                    ambientColor = ButtonThicknessColor
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ButtonTopColor, ButtonBottomColor)
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // Remove default ripple for hardware feel
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Button Label (Play, Stop, Rec)
                Text(
                    text = label,
                    color = if (isActive) activeColor else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                // LED Strip
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isActive) activeColor else LedOffColor)
                        // Simulate LED Glow using Compose Shadow
                        .then(
                            if (isActive) {
                                Modifier.shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(2.dp),
                                    ambientColor = activeColor,
                                    spotColor = activeColor
                                )
                            } else Modifier
                        )
                )
            }
        }
    }
}

package com.example.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.isActive

@Composable
fun RacingScreen(
    viewModel: CarPolyzzViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Main 3D Canvas
        GameCanvas(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )

        // Floating Close Exit button at top-center for manual aborts
        IconButton(
            onClick = { viewModel.finishRaceSummary() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .testTag("exit_race_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Abort Race",
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }

        // --- LOWER FLUID CONTROLLER BUTTON LAYOUTS ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        ) {
            // Steering arrows on Bottom Left side
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left pedal
                ControllerPad(
                    text = "◀ STEER L",
                    onPressState = { pressed -> viewModel.setSteerLeft(pressed) },
                    buttonColor = ThemeSecondary,
                    tag = "steer_left_btn"
                )

                // Right pedal
                ControllerPad(
                    text = "STEER R ▶",
                    onPressState = { pressed -> viewModel.setSteerRight(pressed) },
                    buttonColor = ThemeSecondary,
                    tag = "steer_right_btn"
                )
            }

            // Acceleration & brake sliders on Bottom Right side
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Brake pedal
                ControllerPad(
                    text = "DECEL",
                    onPressState = { pressed -> viewModel.setBrake(pressed) },
                    buttonColor = AccentRed,
                    tag = "brake_paddle_btn"
                )

                // Gas pedal
                ControllerPad(
                    text = "GAS / ACCEL",
                    onPressState = { pressed -> viewModel.setAccelerate(pressed) },
                    buttonColor = ThemePrimary,
                    tag = "gas_paddle_btn"
                )
            }
        }
    }
}

@Composable
fun ControllerPad(
    text: String,
    onPressState: (Boolean) -> Unit,
    buttonColor: Color,
    tag: String,
    modifier: Modifier = Modifier
) {
    // Remembers pressed internal state purely for visual ripple feedback scaling
    val isPressed = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(width = 82.dp, height = 75.dp)
            .background(
                color = if (isPressed.value) buttonColor else buttonColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = if (isPressed.value) buttonColor else buttonColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // Wait for pointer down trigger
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed.value = true
                        onPressState(true)

                        // Wait for lift release trigger
                        waitForUpOrCancellation()
                        isPressed.value = false
                        onPressState(false)
                    }
                }
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = if (isPressed.value) Color.Black else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

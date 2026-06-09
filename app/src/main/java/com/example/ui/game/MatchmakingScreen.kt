package com.example.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MatchmakingScreen(
    viewModel: CarPolyzzViewModel,
    modifier: Modifier = Modifier
) {
    val progress = viewModel.matchmakingProgress
    val opponents = viewModel.botNames
    val statusText = viewModel.matchmakingStatusText
    val selectedTrack = viewModel.selectedTrack

    // Animation variables for matchmaking radar pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Racing Segment Indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SYNCHRONIZING LOBBY...",
                    color = ThemeSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = selectedTrack.name.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Radar Scan visual graphic
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding glowing ripple circles
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(scaleAnim)
                        .alpha(alphaAnim)
                        .border(1.5.dp, ThemePrimary, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(scaleAnim * 0.7f)
                        .alpha(alphaAnim * 0.8f)
                        .border(1.2.dp, ThemeSecondary, CircleShape)
                )

                // Static center target circle
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(DarkSurface, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛰️",
                        fontSize = 18.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dynamic real-time connection status text
                Text(
                    text = statusText.uppercase(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Level matching queue slider
                LinearProgressIndicator(
                    progress = progress,
                    color = ThemePrimary,
                    trackColor = AccentMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Drafted symmetrical opponents placard list
                if (opponents.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "IDENTICAL PERFORMANCE OPPONENTS MATCHED:",
                            color = AccentMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 135.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            opponents.forEachIndexed { idx, name ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Symmetrical active status dot
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 10.dp)
                                                .size(6.dp)
                                                .background(ThemePrimary, CircleShape)
                                        )
                                        Text(
                                            text = name.uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(ThemeSecondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .border(1.dp, ThemeSecondary.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PING: ${25 + idx * 8}MS",
                                            color = ThemeSecondary,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cancellation route
            OutlinedButton(
                onClick = { viewModel.cancelMatchmaking() },
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("matchmaking_cancel_button")
            ) {
                Text(
                    text = "CANCEL LOCK & LEAVE LOBBY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

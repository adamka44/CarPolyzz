package com.example.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RankTier
import com.example.ui.theme.*

@Composable
fun PostRaceSummaryScreen(
    viewModel: CarPolyzzViewModel,
    modifier: Modifier = Modifier
) {
    val placement = viewModel.finalPlacement
    val ptsDelta = viewModel.pointsChangeByRace
    val bestLap = viewModel.currentBestLapFormatted
    val totalTime = viewModel.totalTimeFormatted
    val track = viewModel.selectedTrack
    val perfectCorners = viewModel.totalPerfectCornersInRace
    val userProfile = viewModel.currentUserProfile.value

    // Determine current point standings and tier
    val currentPoints = userProfile?.rankingPoints ?: 0
    val currentTier = RankTier.fromPoints(currentPoints)
    val nextTier = RankTier.getNextTier(currentTier)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "RACE CONCLUDED - METRIC TELEMETRY",
                color = ThemeSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Large standing placement header
            val standingText = when (placement) {
                1 -> "🏆 1ST PLACE - GOLD MEDAL"
                2 -> "🥈 2ND PLACE - SILVER MEDAL"
                3 -> "🥉 3RD PLACE - BRONZE MEDAL"
                else -> "🏁 4TH PLACE - OVER LIMIT"
            }
            val standingColor = when (placement) {
                1 -> ThemePrimary
                2 -> ThemeSecondary
                3 -> AccentGold
                else -> Color.Gray
            }

            Box(
                modifier = Modifier
                    .fillModifierWithGlow()
                    .background(DarkSurface, RoundedCornerShape(24.dp))
                    .border(1.dp, standingColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = standingText,
                        color = standingColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (ptsDelta >= 0) "+$ptsDelta RANK RATING POINTS" else "$ptsDelta RANK RATING POINTS",
                        color = if (ptsDelta >= 0) ThemePrimary else AccentRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Match timings and technical stats list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "CONSOLIDATED ANALYSIS - ${track.name.uppercase()}",
                        color = AccentMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MetricRow(label = "TOTAL RACE LENGTH", value = "9,000 METERS")
                    MetricRow(label = "TOTAL DURATION", value = totalTime)
                    MetricRow(label = "BEST APEX LAP CLOCK", value = bestLap, highlight = true)
                    MetricRow(label = "PERFECT TECHNICAL APEX CORNERS", value = "$perfectCorners")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic ranking progress bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "COMPETITIVE PROGRESSIVE STANDARDS",
                        color = AccentMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT TIER: ${currentTier.displayName.uppercase()}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        if (nextTier != null) {
                            Text(
                                text = "NEXT: ${nextTier.displayName.uppercase()}",
                                color = ThemeSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        } else {
                            Text(
                                text = "MAX COMPETITIVE RANK ACHIEVED",
                                color = ThemePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (nextTier != null) {
                        val currentTierMaxOrMin = currentTier.minPoints
                        val nextTierPoints = nextTier.minPoints
                        val pointsProgress = currentPoints - currentTierMaxOrMin
                        val pointsTotalRangeForTier = nextTierPoints - currentTierMaxOrMin
                        val progressRatio = (pointsProgress.toFloat() / pointsTotalRangeForTier).coerceIn(0f, 1f)

                        LinearProgressIndicator(
                            progress = progressRatio,
                            color = ThemePrimary,
                            trackColor = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${nextTier.minPoints - currentPoints} PTS TO UPGRADE TIER",
                            color = AccentMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = 1.0f,
                            color = ThemePrimary,
                            trackColor = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Large Submit and return button (Artistic Flair Spec Active Button)
        Button(
            onClick = { viewModel.finishRaceSummary() },
            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("summary_continue_btn")
        ) {
            Text(
                text = "SUBMIT TELEMETRY & DISCONNECT",
                color = DarkBackground,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = AccentMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = if (highlight) ThemePrimary else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// Simple layout extension to provide clean structural separation
private fun Modifier.fillModifierWithGlow(): Modifier {
    return this.fillMaxWidth()
}

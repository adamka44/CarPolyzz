package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvas(
    viewModel: CarPolyzzViewModel,
    modifier: Modifier = Modifier
) {
    val speed = viewModel.playerSpeedKmh
    val lap = viewModel.playerLap
    val distance = viewModel.playerProgressMeters
    val playerX = viewModel.playerX
    val track = viewModel.selectedTrack
    val totalRaceTimeMs = viewModel.totalRaceTimeMs
    val isOffRoad = viewModel.isOffRoad
    val boostCharge = viewModel.boostCharge
    val isBoost = viewModel.isBoostActive
    val boostTimer = viewModel.boostTimerLeft
    val alerts = viewModel.speedwayAlertText
    val currentSecName = viewModel.currentSegmentName
    val botDists = viewModel.botDistances
    val botNames = viewModel.botNames
    val boostsOnTrack = viewModel.boostPickupsOnTrack

    // Find current active segment curvature to project curve bend
    val totalLength = track.lapLengthMeters
    var accum = 0f
    var activeCurve = 0f
    for (seg in track.segments) {
        if (distance >= accum && distance < accum + seg.lengthMeters) {
            activeCurve = seg.curvature
            break
        }
        accum += seg.lengthMeters
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Core 3D perspective simulator canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { /* standard tap */ }
                }
        ) {
            val w = size.width
            val h = size.height

            val horizonY = h * 0.45f

            // 1. Draw Sky (Celestial/Neon gradient)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(track.skyColor, DarkBackground),
                    startY = 0f,
                    endY = horizonY
                ),
                size = Size(w, horizonY)
            )

            // Draw Parallax low-poly mountains
            val parallaxOffset = -(playerX * 120f) - (activeCurve * 250f)
            
            // Draw Mountain Left
            val m1Path = Path().apply {
                moveTo(w * 0.1f + parallaxOffset, horizonY)
                lineTo(w * 0.35f + parallaxOffset, horizonY - 140f)
                lineTo(w * 0.6f + parallaxOffset, horizonY)
                close()
            }
            drawPath(path = m1Path, color = track.skyColor.copy(alpha = 0.4f))

            // Draw Mountain Right
            val m2Path = Path().apply {
                moveTo(w * 0.45f + parallaxOffset, horizonY)
                lineTo(w * 0.75f + parallaxOffset, horizonY - 180f)
                lineTo(w * 0.95f + parallaxOffset, horizonY)
                close()
            }
            drawPath(path = m2Path, color = track.skyColor.copy(alpha = 0.5f))

            // 2. Draw Ground
            drawRect(
                color = track.groundColor,
                topLeft = Offset(0f, horizonY),
                size = Size(w, h - horizonY)
            )

            // 3. Draw Projected curved road trapezoid
            // Camera turn bend factor
            val curveFactor = activeCurve * 240f
            val roadHorizonCenter = w * 0.5f + curveFactor
            val roadBottomCenter = w * 0.5f - (playerX * w * 0.22f)

            val horizonRoadWidth = w * 0.12f
            val bottomRoadWidth = w * 0.94f

            val hL = roadHorizonCenter - horizonRoadWidth / 2f
            val hR = roadHorizonCenter + horizonRoadWidth / 2f
            val bL = roadBottomCenter - bottomRoadWidth / 2f
            val bR = roadBottomCenter + bottomRoadWidth / 2f

            // Ground Track Road fill
            val roadPath = Path().apply {
                moveTo(hL, horizonY)
                lineTo(hR, horizonY)
                lineTo(bR, h)
                lineTo(bL, h)
                close()
            }
            drawPath(path = roadPath, color = track.roadColor)

            // 4. Alternating Scrolling Neon Stripes (Rumble blocks)
            // Driven by distance modulo to create moving speed appearance
            val stripeStep = 30f // meters per stripe block
            val scrollOffset = distance % stripeStep
            val stripeCount = 14
            for (i in 0..stripeCount) {
                // Calculate projected Y on exponential curve
                val t = (i * stripeStep - scrollOffset) / (stripeCount * stripeStep)
                if (t < 0f || t > 1.0f) continue

                // Projecting t (exponential perspective factor)
                // t=0 at horizon, t=1 at bottom
                val py = horizonY + (h - horizonY) * t * t

                // Project wide boundaries at this slice
                val pCenter = roadHorizonCenter + (roadBottomCenter - roadHorizonCenter) * t
                val pWidth = horizonRoadWidth + (bottomRoadWidth - horizonRoadWidth) * t

                val leftBoundaryX = pCenter - pWidth / 2f
                val rightBoundaryX = pCenter + pWidth / 2f

                val stripeHeight = (6f + 30f * t).dp.toPx()
                val stripeColor = if (i % 2 == 0) track.stripeColor else Color.White

                // Left stripe block
                drawCircle(
                    color = stripeColor,
                    radius = (1f + 10f * t).dp.toPx(),
                    center = Offset(leftBoundaryX, py)
                )

                // Right stripe block
                drawCircle(
                    color = stripeColor,
                    radius = (1f + 10f * t).dp.toPx(),
                    center = Offset(rightBoundaryX, py)
                )

                // Draw dashed line center division
                if (i % 2 == 0) {
                    val centerLineW = (1f + 4f * t).dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(pCenter, py - stripeHeight/2),
                        end = Offset(pCenter, py + stripeHeight/2),
                        strokeWidth = centerLineW
                    )
                }
            }

            // 5. Draw Lane Speed Booster items
            for (p in boostsOnTrack) {
                // Relative distance to player
                val distAhead = p.distanceMeters - distance
                // Show if ahead and within 120 meters perspective sphere
                if (distAhead in 0f..140f && !p.collected) {
                    val t = 1.0f - (distAhead / 140f) // t values: 0 at far horizon, 1 at near front
                    val py = horizonY + (h - horizonY) * t * t

                    val pCenter = roadHorizonCenter + (roadBottomCenter - roadHorizonCenter) * t
                    val pWidth = horizonRoadWidth + (bottomRoadWidth - horizonRoadWidth) * t

                    // Lane position offset
                    val itemX = pCenter + (p.laneX * pWidth * 0.42f)
                    val sizeFactor = (4f + 32f * t).dp.toPx()

                    // Glow circle
                    drawCircle(
                        color = ThemePrimary.copy(alpha = 0.4f * t),
                        radius = sizeFactor * 1.6f,
                        center = Offset(itemX, py)
                    )

                    // Draw Low-Poly Diamond Prism
                    val dPath = Path().apply {
                        moveTo(itemX, py - sizeFactor) // top
                        lineTo(itemX - sizeFactor * 0.7f, py) // left
                        lineTo(itemX, py + sizeFactor * 1.2f) // bottom
                        lineTo(itemX + sizeFactor * 0.7f, py) // right
                        close()
                    }
                    drawPath(path = dPath, color = ThemePrimary)

                    // Drawing geometric inner diamond line
                    drawLine(
                        color = Color.White,
                        start = Offset(itemX, py - sizeFactor),
                        end = Offset(itemX, py + sizeFactor * 1.2f),
                        strokeWidth = (1f + 2f * t).dp.toPx()
                    )
                }
            }

            // 6. Draw AI Competitors (PROJECTED BOTS)
            val playerDistance = distance
            for (bIndex in 0..8) {
                val botDist = botDists[bIndex]
                val bAhead = botDist - playerDistance
                
                // Render if within 110m ahead
                if (bAhead in 0f..110f) {
                    val t = 1.0f - (bAhead / 110f)
                    val py = horizonY + (h - horizonY) * t * t

                    val pCenter = roadHorizonCenter + (roadBottomCenter - roadHorizonCenter) * t
                    val pWidth = horizonRoadWidth + (bottomRoadWidth - horizonRoadWidth) * t

                    // Bot drifts slightly left/right dynamically
                    val botSwingX = (sin(((totalRaceTimeMs / 600.0) + bIndex * 1.3)).toFloat() * 0.35f)
                    val itemX = pCenter + (botSwingX * pWidth * 0.42f)

                    val cw = (12f + 85f * t).dp.toPx() // vehicle projected width
                    val ch = (5f + 35f * t).dp.toPx()  // vehicle projected height

                    // Custom Low poly car chassis shape
                    val carPath = Path().apply {
                        moveTo(itemX - cw/2f, py)
                        lineTo(itemX - cw * 0.35f, py - ch)
                        lineTo(itemX + cw * 0.35f, py - ch)
                        lineTo(itemX + cw/2f, py)
                        close()
                    }
                    drawPath(
                        path = carPath,
                        color = when (bIndex % 4) {
                            0 -> Color(0xFFFF5252) // red
                            1 -> ThemeSecondary   // cyan
                            2 -> Color(0xFFE040FB) // magenta
                            else -> Color(0xFFFFEB3B) // yellow
                        }
                    )

                    // Drawing Windshield glass of bot car
                    val windPath = Path().apply {
                        moveTo(itemX - cw*0.25f, py - ch)
                        lineTo(itemX - cw*0.15f, py - ch * 0.4f)
                        lineTo(itemX + cw*0.15f, py - ch * 0.4f)
                        lineTo(itemX + cw*0.25f, py - ch)
                        close()
                    }
                    drawPath(path = windPath, color = Color.White.copy(alpha = 0.8f))

                    // Wheels
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(itemX - cw * 0.45f, py - ch * 0.3f),
                        size = Size(cw * 0.12f, ch * 0.5f)
                    )
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(itemX + cw * 0.33f, py - ch * 0.3f),
                        size = Size(cw * 0.12f, ch * 0.5f)
                    )

                    // Floating bot text ID placard
                    val labelY = py - ch - (2f + 8f * t).dp.toPx()
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.6f * t),
                        radius = (5f + 12f * t).dp.toPx(),
                        center = Offset(itemX, labelY)
                    )
                }
            }

            // 7. Render Player CAR COCKPIT (Fixed Third-person view bottom center)
            // Slightly offset/shake based on speed and offroad bump vibration!
            val speedShakeX = if (isOffRoad) (Random.nextFloat() * 10f - 5f) else 0f
            val myCarX = w * 0.5f + speedShakeX
            val myCarY = h * 0.86f + (if (isOffRoad) (Random.nextFloat() * 6f - 3f) else 0f)

            val baseW = 190.dp.toPx()
            val baseH = 75.dp.toPx()

            // Rear bumper polygon
            val bumperPath = Path().apply {
                moveTo(myCarX - baseW * 0.42f, myCarY + baseH * 0.7f)
                lineTo(myCarX - baseW * 0.36f, myCarY)
                lineTo(myCarX + baseW * 0.36f, myCarY)
                lineTo(myCarX + baseW * 0.42f, myCarY + baseH * 0.7f)
                close()
            }
            drawPath(path = bumperPath, color = ThemeSecondary)

            // Windshield and Cabin cover
            val cabinPath = Path().apply {
                moveTo(myCarX - baseW * 0.28f, myCarY)
                lineTo(myCarX - baseW * 0.12f, myCarY - baseH * 0.82f)
                lineTo(myCarX + baseW * 0.12f, myCarY - baseH * 0.82f)
                lineTo(myCarX + baseW * 0.28f, myCarY)
                close()
            }
            drawPath(path = cabinPath, color = Color(0xFF0F172A))
            
            // Neon cyan glowing glass
            val glassPath = Path().apply {
                moveTo(myCarX - baseW * 0.22f, myCarY - 5f)
                lineTo(myCarX - baseW * 0.10f, myCarY - baseH * 0.75f)
                lineTo(myCarX + baseW * 0.10f, myCarY - baseH * 0.75f)
                lineTo(myCarX + baseW * 0.22f, myCarY - 5f)
                close()
            }
            drawPath(path = glassPath, color = ThemeSecondary.copy(alpha = 0.5f))

            // Spoiler struts / Wings
            val mySpoilerPath = Path().apply {
                moveTo(myCarX - baseW * 0.52f, myCarY - baseH * 0.3f)
                lineTo(myCarX + baseW * 0.52f, myCarY - baseH * 0.3f)
                lineTo(myCarX + baseW * 0.46f, myCarY - baseH * 0.12f)
                lineTo(myCarX - baseW * 0.46f, myCarY - baseH * 0.12f)
                close()
            }
            drawPath(path = mySpoilerPath, color = Color.Black)

            // Spoilers wing
            drawRect(
                color = ThemePrimary,
                topLeft = Offset(myCarX - baseW * 0.56f, myCarY - baseH * 0.45f),
                size = Size(baseW * 1.12f, baseH * 0.15f)
            )

            // Rear taillights neon glow
            drawRect(
                color = if (viewModel.setBrake(false).run { true }) AccentRed else Color(0xAA6E1B1B),
                topLeft = Offset(myCarX - baseW * 0.35f, myCarY + 8f),
                size = Size(35f, 15f)
            )
            drawRect(
                color = if (viewModel.setBrake(false).run { true }) AccentRed else Color(0xAA6E1B1B),
                topLeft = Offset(myCarX + baseW * 0.35f - 35f, myCarY + 8f),
                size = Size(35f, 15f)
            )

            // Neon Exhaust boost flames when active boost is on!
            if (isBoost) {
                val fW = 40f
                val fH = 75f
                val firePathLeft = Path().apply {
                    moveTo(myCarX - baseW * 0.15f, myCarY + baseH * 0.75f)
                    lineTo(myCarX - baseW * 0.15f - fW, myCarY + baseH * 0.75f + fH + (Random.nextFloat() * 20f))
                    lineTo(myCarX - baseW * 0.15f + fW, myCarY + baseH * 0.75f + fH / 2)
                    close()
                }
                drawPath(path = firePathLeft, color = Color(0xFFFFD600))

                val firePathRight = Path().apply {
                    moveTo(myCarX + baseW * 0.15f, myCarY + baseH * 0.75f)
                    lineTo(myCarX + baseW * 0.15f - fW, myCarY + baseH * 0.75f + fH / 2)
                    lineTo(myCarX + baseW * 0.15f + fW, myCarY + baseH * 0.75f + fH + (Random.nextFloat() * 20f))
                    close()
                }
                drawPath(path = firePathRight, color = Color(0xFFFF7A00))
            }
        }

        // --- GAME INTERACTIVE HUD & ALERTS ---

        // Race notifications (e.g. READY/GO / BUMP / DRIFT PERFECT)
        if (alerts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 130.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = alerts,
                    color = if (alerts.contains("OFF")) AccentRed else ThemePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Live HUD details overlay at Top-Left (Laps, segment, clock)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "TRACK: ${track.name.uppercase()}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ZONE: $currentSecName",
                color = ThemeSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "LAP: $lap / 3",
                color = ThemePrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "TIME: ${viewModel.formatGameDuration(viewModel.totalRaceTimeMs)}",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Mini tracking HUD at Top-Right showing racer live progression meters!
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
                .width(130.dp)
        ) {
            Text(
                text = "COMPETITORS",
                color = ThemeSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Build vertical progress bars
            // User progress
            val uProgress = (lap - 1) * totalLength + distance
            val targetTotalDist = 3 * totalLength
            
            // Map list of pairs of names to total meters (Dynamically scale to 10 spots)
            val listBuilder = mutableListOf("YOU" to uProgress)
            for (i in 0..8) {
                val name = botNames.getOrNull(i)?.take(8) ?: "Bot $i"
                val dist = botDists.getOrNull(i) ?: 0f
                listBuilder.add(name to dist)
            }
            val scoresList = listBuilder.sortedByDescending { it.second }

            scoresList.forEachIndexed { idx, pair ->
                val ratio = (pair.second / targetTotalDist).coerceIn(0f, 1.0f)
                val isMe = pair.first == "YOU"
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${idx+1}. ${pair.first}",
                        color = if (isMe) ThemePrimary else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio)
                                .background(if (isMe) ThemePrimary else Color.White)
                        )
                    }
                }
            }
        }

        // Speedometer UI Dashboard (Bottom Right overlaying dashboard)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.82f), shape = RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                // Digital large speedometer
                Text(
                    text = "${speed.toInt()}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "KM/H",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(25.dp))
            ) {
                // Circle RPM indicator progress
                val rpmRatio = (speed / 230f).coerceIn(0.1f, 1.0f)
                CircularProgressIndicator(
                    progress = rpmRatio,
                    color = if (isBoost) ThemeSecondary else ThemePrimary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "D${((speed / 40f).toInt() + 1).coerceAtMost(6)}",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }

        // Floating BOOST charging orb trigger button at lower left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 125.dp, start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val boostReady = boostCharge >= 100f
            Button(
                onClick = { viewModel.triggerActiveBoost() },
                enabled = boostReady || isBoost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBoost) Color.Red else ThemePrimary,
                    disabledContainerColor = AccentMuted
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        progress = boostCharge / 100f,
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = if (isBoost) "BOOST!" else "${boostCharge.toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Text(
                text = "NITRO BOOST",
                color = if (boostReady) ThemePrimary else Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

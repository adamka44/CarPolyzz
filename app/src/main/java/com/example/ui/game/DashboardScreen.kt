package com.example.ui.game

import androidx.compose.foundation.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.RaceRecord
import com.example.data.model.LeaderboardEntry
import com.example.data.model.RankTier
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: CarPolyzzViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val records by viewModel.personalRecords.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val selectedTrack = viewModel.selectedTrack

    var activeTab by remember { mutableStateOf(0) } // 0: Race Station, 1: Leaderboard, 2: Personal Records

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Sleek Account identity header bar
        userProfile?.let { profile ->
            UserHeaderBar(
                profile = profile,
                onLogout = { viewModel.signOut() }
            )
        }

        // 2. Tab Navigation Selector Bar (Artistic Flair Custom Styled)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("RACE STATION", "LEADERBOARD", "RACE LABS")
            tabs.forEachIndexed { index, title ->
                val isActive = activeTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = index }
                        .padding(vertical = 8.dp)
                        .testTag("tab_${title.lowercase().replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(3.dp)
                                    .background(ThemePrimary, RoundedCornerShape(1.5.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(3.dp)
                                    .background(Color.Transparent)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = title,
                            color = if (isActive) ThemePrimary else AccentMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Central Dynamic Body Panes
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> RaceStationPane(
                    viewModel = viewModel,
                    selectedTrack = selectedTrack
                )
                1 -> LeaderboardPane(
                    leaderboard = leaderboard,
                    currentUser = userProfile
                )
                2 -> RecordsPane(
                    viewModel = viewModel,
                    records = records,
                    profile = userProfile
                )
            }
        }
    }
}

@Composable
fun UserHeaderBar(
    profile: UserProfile,
    onLogout: () -> Unit
) {
    val tier = profile.getRankTier()
    val initials = if (profile.displayName.isNotEmpty()) profile.displayName.take(1).uppercase() else "P"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "VERIFIED DRIVER",
                color = AccentMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.displayName.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Tiny Log Out trigger text button
                Text(
                    text = "DISCONNECT",
                    color = AccentRed.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable { onLogout() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Beautiful custom translucent green avatar bubble matching verified badge
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(ThemePrimary.copy(alpha = 0.1f), RoundedCornerShape(23.dp))
                .border(1.dp, ThemePrimary.copy(alpha = 0.3f), RoundedCornerShape(23.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = ThemePrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun RaceStationPane(
    viewModel: CarPolyzzViewModel,
    selectedTrack: Track
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CHOOSE LONG-FORM STAGE",
            color = AccentMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        )

        // DYNAMIC SHAPE GENERATOR PANEL
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(
                    width = 1.dp,
                    color = ThemePrimary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔬 SHAPE LABS: DYNAMIC CIRCUIT INVENTOR",
                    color = ThemePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Formulate completely randomized, closed-loop track layouts with custom curvatures, segment counts, and aesthetic styles.",
                    color = AccentMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Oval generator button
                    Button(
                        onClick = { 
                            val randTrack = Tracks.generateRandomTrack("oval")
                            viewModel.selectTrack(randTrack)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeSecondary.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, ThemeSecondary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("OVAL", color = ThemeSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Fast Loop", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    
                    // Triangle generator button
                    Button(
                        onClick = { 
                            val randTrack = Tracks.generateRandomTrack("triangle")
                            viewModel.selectTrack(randTrack)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TRIANGLE", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("3 Apex turns", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    
                    // Serpentine generator button
                    Button(
                        onClick = { 
                            val randTrack = Tracks.generateRandomTrack("serpentine")
                            viewModel.selectTrack(randTrack)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SERPENT", color = ThemePrimary, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("S-Curves", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Tracks List Cards selection
        Tracks.list.forEach { track ->
            val isSelected = track.id == selectedTrack.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.selectTrack(track) }
                    .border(
                        width = 1.dp,
                        color = if (isSelected) ThemePrimary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DarkSurface else DarkSurface.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small aesthetic terrain visual circle indicator
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(track.groundColor, RoundedCornerShape(10.dp))
                            .border(1.dp, track.roadColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .align(Alignment.BottomCenter)
                                .background(track.roadColor)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = track.name.uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (track.difficulty) {
                                            "Medium" -> ThemeSecondary.copy(alpha = 0.15f)
                                            "Hard" -> AccentRed.copy(alpha = 0.15f)
                                            else -> ThemePrimary.copy(alpha = 0.15f)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = track.difficulty.uppercase(),
                                    color = when (track.difficulty) {
                                        "Medium" -> ThemeSecondary
                                        "Hard" -> AccentRed
                                        else -> ThemePrimary
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = track.description,
                            color = AccentMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(modifier = Modifier.padding(top = 6.dp)) {
                            Text(
                                text = "LAP LENGTH: ${(track.lapLengthMeters / 1000).toInt()}KM",
                                color = ThemeSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "•  3-LAP STRUCTURED",
                                color = AccentMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Selected track details pane (Recreating precision HTML layout)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "STAGE PATH ANALYSIS: ${selectedTrack.name.uppercase()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Three-Column Spec Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Col 1: Laps
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(text = "RACE LAPS", color = AccentMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "03 LAPS", color = ThemePrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    // Divider
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                    // Col 2: Distance
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TOTAL TRACK", color = AccentMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "${String.format("%.1f", selectedTrack.lapLengthMeters * 3 / 1000f)} KM", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    // Divider
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                    // Col 3: Sector Elements
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(text = "TURNS COUNT", color = AccentMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        val turnCount = selectedTrack.segments.count { it.curvature != 0f }
                        Text(text = String.format("%02d SECTORS", turnCount), color = ThemeSecondary, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                Spacer(modifier = Modifier.height(12.dp))

                // Render dynamic track segment breakdown
                selectedTrack.segments.forEach { seg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        when {
                                            seg.curvature > 0.1f -> AccentRed
                                            seg.curvature < -0.1f -> ThemeSecondary
                                            else -> ThemePrimary
                                        },
                                        RoundedCornerShape(3.5.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = seg.name,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${seg.lengthMeters.toInt()}m (${
                                when {
                                    seg.curvature > 0.1f -> "Right Curve"
                                    seg.curvature < -0.1f -> "Left Curve"
                                    else -> "Straightway"
                                }
                            })",
                            color = AccentMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large matchmaking trigger button (Artistic Flair 2rem styling)
        Button(
            onClick = { viewModel.startMatchmaking() },
            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("race_trigger_button")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Race icon",
                tint = DarkBackground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ENTER MATCHMAKING QUEUE",
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
fun LeaderboardPane(
    leaderboard: List<LeaderboardEntry>,
    currentUser: UserProfile?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // competitive tier rules disclaimer details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, ThemeSecondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Competition info",
                    tint = ThemeSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Leaderboard matches update with direct P2P ranking formula. 1st rewards +45 points while 4th deducts -15 points. Clean control keys yield bonus multipliers.",
                    color = AccentMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leaderboard.size) { index ->
                val entry = leaderboard[index]
                val rankPosition = index + 1
                val isMe = entry.id == currentUser?.id
                val tier = entry.getRankTier()
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isMe) ThemePrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) DarkSurface else DarkSurface.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Custom Podiums Badge (Artistic Flair Spec)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        when (rankPosition) {
                                            1 -> AccentGold.copy(alpha = 0.12f)
                                            2 -> ThemeSecondary.copy(alpha = 0.12f)
                                            3 -> ThemeTertiary.copy(alpha = 0.12f)
                                            else -> Color.White.copy(alpha = 0.03f)
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when (rankPosition) {
                                            1 -> AccentGold.copy(alpha = 0.4f)
                                            2 -> ThemeSecondary.copy(alpha = 0.4f)
                                            3 -> ThemeTertiary.copy(alpha = 0.4f)
                                            else -> Color.White.copy(alpha = 0.08f)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$rankPosition",
                                    color = when (rankPosition) {
                                        1 -> AccentGold
                                        2 -> ThemeSecondary
                                        3 -> ThemeTertiary
                                        else -> Color.White
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.username.uppercase(),
                                        color = if (isMe) ThemePrimary else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (isMe) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 6.dp)
                                                .background(ThemePrimary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "YOU",
                                                color = ThemePrimary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "RACES COMPLETED: ${entry.racesCompleted}",
                                        color = AccentMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Rating badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color(android.graphics.Color.parseColor(tier.colorHex)).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(android.graphics.Color.parseColor(tier.colorHex)).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tier.displayName.uppercase(),
                                    color = Color(android.graphics.Color.parseColor(tier.colorHex)),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${entry.points} PTS",
                                color = if (isMe) ThemePrimary else Color.White,
                                fontSize = 12.sp,
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

@Composable
fun RecordsPane(
    viewModel: CarPolyzzViewModel,
    records: List<RaceRecord>,
    profile: UserProfile?
) {
    var subTab by remember { mutableStateOf(0) } // 0: ONLINE HUB, 1: CAREER TELEMETRY
    val tier = profile?.getRankTier() ?: RankTier.BRONZE
    val ordinal = tier.ordinal + 1
    val ordinalText = if (ordinal < 10) "0$ordinal" else "$ordinal"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (subTab != 0) Modifier.verticalScroll(rememberScrollState())
                else Modifier
            )
            .padding(20.dp)
    ) {
        // Inner Sub-Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(DarkSurface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val subTabs = listOf("ONLINE HUB", "CAREER TELEMETRY")
            subTabs.forEachIndexed { index, label ->
                val isSelected = subTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) ThemePrimary.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) ThemePrimary.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { subTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) ThemePrimary else AccentMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (subTab == 0) {
            // ONLINE MULTIPLAYER HUB
            val chatMessages by viewModel.chatMessages.collectAsState()
            var textInput by remember { mutableStateOf("") }
            val chatScrollState = rememberScrollState()

            // Keep scrolled to bottom whenever a new chat message arrives
            LaunchedEffect(chatMessages.size) {
                chatScrollState.animateScrollTo(chatScrollState.maxValue)
            }

            // Regional servers card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "P2P REGIONAL NODES",
                            color = AccentMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(ThemePrimary, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${viewModel.activeDriversCount} DRIVERS ONLINE",
                                color = ThemePrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val regions = listOf(
                            "EU-CENTRAL-1" to "EU",
                            "US-WEST-2" to "US",
                            "ASIA-EAST-1" to "AS"
                        )
                        regions.forEach { (node, label) ->
                            val isSelected = viewModel.selectedRegion == node
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) ThemeSecondary.copy(alpha = 0.12f) else DarkSurface,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) ThemeSecondary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.04f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.selectRegionNode(node) }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ThemeSecondary else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isSelected) "${viewModel.onlineLatencyMs}ms" else "-- ms",
                                        color = if (isSelected) ThemePrimary else AccentMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Chat Board
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "LIVE LOBBY DRIVERS CHAT",
                        color = AccentMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Chats list panel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(chatScrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (chatMessages.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Connecting to chat room...",
                                        color = AccentMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                chatMessages.forEach { msg ->
                                    val rankColor = try {
                                        Color(android.graphics.Color.parseColor(msg.senderRankColorHex))
                                    } catch (e: Exception) {
                                        ThemeSecondary
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (msg.isMe) ThemePrimary.copy(alpha = 0.05f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (msg.isMe) ThemePrimary.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = msg.senderName.uppercase(),
                                                    color = rankColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    text = msg.timestampFormatted,
                                                    color = AccentMuted,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = msg.messageText,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.SansSerif,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Input & Send
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    "Type transmission message...",
                                    fontSize = 11.sp,
                                    color = AccentMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ThemePrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("chat_input_text")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendChatMessage(textInput)
                                    textInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("chat_send_button")
                        ) {
                            Text(
                                text = "SEND",
                                color = DarkBackground,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        } else {
            // CAREER TELEMETRY SECTION
            // 1. Signature Glowing Rank Card (Artistic Flair Layout)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .background(DarkSurface, RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = Color(android.graphics.Color.parseColor(tier.colorHex)).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                // Absolute giant background ordinal text on the right
                Text(
                    text = ordinalText,
                    color = Color.White.copy(alpha = 0.03f),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "CURRENT COMPETITIVE RANK",
                        color = Color(android.graphics.Color.parseColor(tier.colorHex)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = tier.displayName.uppercase(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Rank progress telemetry
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${profile?.rankingPoints ?: 1000} PTS",
                            color = ThemePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• DIRECT P2P STANDING",
                            color = AccentMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Text(
                text = "CAREER STATISTICAL TELEMETRY",
                color = AccentMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Statistics grid cards (Modern low contrast styled borders)
            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "RACES RUN", color = AccentMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile?.racesCompleted ?: 0}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                        .border(1.dp, ThemePrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "VICTORIES (1ST)", color = ThemePrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile?.firstPlaces ?: 0}",
                            color = ThemePrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "PODIUMS (TOP 3)", color = ThemeSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalPod = (profile?.firstPlaces ?: 0) + (profile?.secondPlaces ?: 0) + (profile?.thirdPlaces ?: 0)
                        Text(
                            text = "$totalPod",
                            color = ThemeSecondary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "PERFECT TURNS", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile?.perfectCornersCount ?: 0}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "FASTEST LAP TIME RECORDS",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(DarkSurface.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No race telemetry recorded yet. Complete ranked sessions to write records.",
                        color = AccentMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                records.sortedBy { it.bestLapTimeMs }.forEach { r ->
                    val minutes = (r.bestLapTimeMs / 1000) / 60
                    val seconds = (r.bestLapTimeMs / 1000) % 60
                    val hundredths = (r.bestLapTimeMs % 1000) / 10
                    val lapFormatted = String.format("%02d:%02d.%02d", minutes, seconds, hundredths)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = r.trackName.uppercase(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "STABLE RECORD LAP",
                                    color = AccentMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = lapFormatted,
                                color = ThemePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

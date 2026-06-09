package com.example.ui.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.model.RaceRecord
import com.example.data.model.LeaderboardEntry
import com.example.data.model.RankTier
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

sealed interface ScreenState {
    object Login : ScreenState
    object Dashboard : ScreenState
    object Matchmaking : ScreenState
    object Racing : ScreenState
    object PostRaceSummary : ScreenState
}

class CarPolyzzViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = GameRepository(db.userDao(), db.raceRecordDao(), db.leaderboardDao())

    // UI Screen Navigation State
    var screenState by mutableStateOf<ScreenState>(ScreenState.Login)
        private set

    // Current Google account user
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    // Loaded profile
    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    // Records & Leaderboard state
    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboard
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _personalRecords = MutableStateFlow<List<RaceRecord>>(emptyList())
    val personalRecords = _personalRecords.asStateFlow()

    // Race configurations
    var selectedTrack by mutableStateOf(Tracks.list[0])
        private set

    // Matchmaking variables
    var matchmakingProgress by mutableStateOf(0f)
        private set
    var matchmakingOpponents by mutableStateOf<List<LeaderboardEntry>>(emptyList())
        private set
    var matchmakingStatusText by mutableStateOf("Ready to race")
        private set

    // RACING CORE GAME STATES
    var playerSpeedKmh by mutableStateOf(0f)
    var playerLap by mutableStateOf(1)
    var playerProgressMeters by mutableStateOf(0f)
    var playerX by mutableStateOf(0f) // Center is 0f, Left limit -1.2f, Right limit 1.2f
    var roadCurveOffset by mutableStateOf(0f) // Current direction turn pressure
    
    // Boost system
    var boostCharge by mutableStateOf(0f) // 0 to 100
    var isBoostActive by mutableStateOf(false)
    var boostTimerLeft by mutableStateOf(0f)

    // Statistics during current active race
    var totalRaceTimeMs by mutableStateOf(0L)
    var lapStartTimestamp by mutableStateOf(0L)
    var speedwayAlertText by mutableStateOf("")
    var currentSegmentName by mutableStateOf("")
    var isOffRoad by mutableStateOf(false)
    var perfectCornerStreak by mutableStateOf(0)
    var totalPerfectCornersInRace by mutableStateOf(0)
    var lapTimesList = mutableListOf<String>()

    // Competitors live distances (accumulated total meters) (9 opponents for 10 spots total)
    var botDistances = mutableListOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    var botNames = listOf("PolyBot X", "VectorViper", "ApexMaster", "LowPolyPrism", "FlatShaded", "DrifterX", "Gearbox", "CornerClipper", "BoostFiend")
    var isSlipstreamActive by mutableStateOf(false)

    // Lane visual placement items for boosts (randomly generated)
    // Holds list of pair of: Segment progress percent (0.0 to 1.0) and Target Lane (-0.8f, 0.0f, 0.8f)
    var boostPickupsOnTrack = mutableListOf<BoostPickupItem>()

    // Post-Race summary details
    var finalPlacement by mutableStateOf(4)
    var pointsChangeByRace by mutableStateOf(0)
    var currentBestLapFormatted by mutableStateOf("00:00.00")
    var totalTimeFormatted by mutableStateOf("00:00.00")

    // Online Multiplayer Hub States
    var selectedRegion by mutableStateOf("EU-CENTRAL-1")
    var onlineLatencyMs by mutableStateOf(42)
    var activeDriversCount by mutableStateOf(142)
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    // Steering held state helpers
    private var steerLeftHeld = false
    private var steerRightHeld = false
    private var accelerateHeld = false
    private var brakeHeld = false

    private var gameLoopJob: Job? = null
    private var matchmakingJob: Job? = null

    init {
        // Seeds the default leaderboard instantly
        viewModelScope.launch {
            repository.seedDefaultLeaderboard()
        }
        setupInitialChat()
        startLiveOnlineSimulation()
    }

    private fun setupInitialChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = "init_1",
                senderName = "DrifterX",
                senderRankColorHex = "#A855F7",
                messageText = "Just hit a sub-45s lap on Neon Grid! Tires were absolute fire 🔥",
                timestampFormatted = "3M AGO",
                isMe = false
            ),
            ChatMessage(
                id = "init_2",
                senderName = "AsphaltLegend",
                senderRankColorHex = "#FBBF24",
                messageText = "Anyone up for some 3-lap action on Emerald Oasis? LFG!",
                timestampFormatted = "1M AGO",
                isMe = false
            ),
            ChatMessage(
                id = "init_3",
                senderName = "LowPolyPrism",
                senderRankColorHex = "#10B981",
                messageText = "Server latency is super low today, direct P2P sync is crisp.",
                timestampFormatted = "JUST NOW",
                isMe = false
            )
        )
    }

    private fun startLiveOnlineSimulation() {
        viewModelScope.launch {
            while (isActive) {
                delay(kotlin.random.Random.nextLong(15000, 25000))
                
                // Fluctuating active count and ping slightly to feel live
                activeDriversCount = (activeDriversCount + kotlin.random.Random.nextInt(-5, 6)).coerceIn(120, 280)
                onlineLatencyMs = (onlineLatencyMs + kotlin.random.Random.nextInt(-3, 4)).coerceIn(28, 65)

                val drivers = listOf("Gearbox", "ApexMaster", "CornerClipper", "BoostFiend", "Oversteer", "FlatShaded", "VectorViper")
                val colors = listOf("#10B981", "#14B8A6", "#F97316", "#EF4444", "#FBBF24")
                val quotes = listOf(
                    "Check out the turn specs on Emerald Oasis, pure drift heaven.",
                    "Matchmaking queue is popping off right now!",
                    "Who has the track record for Neon Grid? Need to beat it.",
                    "Nitro pickups make all the difference, make sure to aim your lines.",
                    "Just ranked up to Silver tier! Let's goooo!",
                    "GG to the driver in last race, that boost speed was insane.",
                    "That off-course speed penalty is brutal, stay on the asphalt!"
                )

                val newChat = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    senderName = drivers.random(),
                    senderRankColorHex = colors.random(),
                    messageText = quotes.random(),
                    timestampFormatted = "JUST NOW",
                    isMe = false
                )
                
                val currentList = _chatMessages.value
                val updatedList = if (currentList.size >= 25) currentList.drop(1) else currentList
                _chatMessages.value = updatedList + newChat
            }
        }
    }

    fun selectRegionNode(region: String) {
        selectedRegion = region
        onlineLatencyMs = when (region) {
            "US-WEST-2" -> 72
            "EU-CENTRAL-1" -> 42
            "ASIA-EAST-1" -> 165
            else -> 90
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val profile = _currentUserProfile.value
        val name = profile?.displayName ?: "Verified Racer"
        val tier = profile?.getRankTier() ?: RankTier.TRASH
        
        val newMsg = ChatMessage(
            id = java.util.UUID.randomUUID().toString(),
            senderName = name,
            senderRankColorHex = tier.colorHex,
            messageText = text,
            timestampFormatted = "JUST NOW",
            isMe = true
        )
        
        _chatMessages.value = _chatMessages.value + newMsg
        
        viewModelScope.launch {
            delay(1500)
            val botAnswers = listOf(
                "Nice pace! Let's match up in queue.",
                "GG! See you on the speedway stream.",
                "Let's race! Join the Matchmaking lobby.",
                "A clean line is the fastest line! Keep driftin'.",
                "Ready to drift. Start matchmaking on Race Station!"
            )
            val botName = listOf("DrifterX", "ApexMaster", "BoostFiend", "PrecisionPilot").random()
            
            val botMsg = ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                senderName = botName,
                senderRankColorHex = "#14B8A6",
                messageText = botAnswers.random(),
                timestampFormatted = "JUST NOW",
                isMe = false
            )
            _chatMessages.value = _chatMessages.value + botMsg
        }
    }

    // Google Sign-In Simulation
    fun signInWithGoogle(email: String, displayName: String) {
        viewModelScope.launch {
            val formattedId = email.lowercase().trim()
            _currentUserId.value = formattedId
            
            // Check if profile exists, otherwise create it
            var profile = repository.getUserProfileSync(formattedId)
            if (profile == null) {
                profile = UserProfile(
                    id = formattedId,
                    email = email,
                    displayName = displayName,
                    avatarUrl = "https://lh3.googleusercontent.com/a/default-user=s120-c",
                    rankingPoints = 0 // Matches the Trash entry level
                )
                repository.insertUser(profile)
            }
            
            _currentUserProfile.value = profile
            
            // Collect records reactive updates
            repository.getRecordsForUser(formattedId).collect { records ->
                _personalRecords.value = records
            }
        }
        screenState = ScreenState.Dashboard
    }

    // Sign out function
    fun signOut() {
        _currentUserId.value = null
        _currentUserProfile.value = null
        screenState = ScreenState.Login
    }

    fun selectTrack(track: Track) {
        selectedTrack = track
    }

    // Matchmaking queue process
    fun startMatchmaking() {
        if (currentUserId.value == null) return
        screenState = ScreenState.Matchmaking
        matchmakingProgress = 0f
        matchmakingStatusText = "Initializing secure peer-to-peer visual channels..."
        
        matchmakingJob = viewModelScope.launch {
            val delayMs = 60L
            val totalTicks = 80
            
            // Determine user rank tier to find realistic matching bots
            val userPoints = _currentUserProfile.value?.rankingPoints ?: 0
            val userTier = RankTier.fromPoints(userPoints)
            
            // Load global leaderboard entries to draft competitors
            delay(800)
            matchmakingStatusText = "Scanning online ${userTier.displayName} competitors..."
            
            // Select 9 random bot competitors matching standard performance
            val pool = listOf(
                "SpeedViper", "ApexMaster", "CarbonKid", "DriftRookie", "AsphaltLegend",
                "GripOverlord", "Chronos", "FormulaPoly", "SlickSteer", "SilverChariot",
                "Octane", "Gearbox", "NoobRider", "DrifterX", "CornerClipper", "BoostFiend",
                "VectorViper", "PrecisionPilot", "Antigravity", "LowPolyPrism", "FlatShaded",
                "ShiftNoob", "Chicanist", "GridRider", "SpeedVector", "MachOne"
            )
            val selectedBots = pool.shuffled().take(9)
            botNames = selectedBots
            
            for (i in 1..totalTicks) {
                delay(delayMs)
                matchmakingProgress = i / totalTicks.toFloat()
                
                when (i) {
                    20 -> {
                        matchmakingStatusText = "Pinging synchronized lobby servers..."
                    }
                    40 -> {
                        matchmakingStatusText = "Equalizing vehicle configurations (Identical performance matching)..."
                    }
                    60 -> {
                        matchmakingStatusText = "Opponents found! Simulating track layouts..."
                    }
                    75 -> {
                        matchmakingStatusText = "Ready to Sync: Entered lobby with 9 online drivers."
                    }
                }
            }
            
            matchmakingStatusText = "LAUNCHING MATCH"
            delay(500)
            initiateActiveRace()
        }
    }

    fun cancelMatchmaking() {
        matchmakingJob?.cancel()
        screenState = ScreenState.Dashboard
        matchmakingStatusText = "Ready to race"
    }

    // Prepare active race parameters
    private fun initiateActiveRace() {
        screenState = ScreenState.Racing
        
        // Setup initial physical indicators
        playerSpeedKmh = 0f
        playerLap = 1
        playerProgressMeters = 0f
        playerX = 0f
        roadCurveOffset = 0f
        boostCharge = 35f
        isBoostActive = false
        boostTimerLeft = 0f
        totalRaceTimeMs = 0L
        lapStartTimestamp = 0L
        perfectCornerStreak = 0
        totalPerfectCornersInRace = 0
        lapTimesList.clear()
        
        // Reset bot racers (9 players, 10 spots)
        botDistances = mutableListOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        
        // Generate randomized cosmetic lane boost placements
        boostPickupsOnTrack.clear()
        val totalTrackLength = selectedTrack.lapLengthMeters
        // Generate 15 boost placements scattered across random lanes
        for (i in 1..15) {
            val dist = i * (totalTrackLength / 16) + Random.nextFloat() * 80f
            val lane = when (Random.nextInt(3)) {
                0 -> -0.8f // Left lane
                1 -> 0.0f  // Center lane
                else -> 0.8f // Right lane
            }
            boostPickupsOnTrack.add(BoostPickupItem(id = i, distanceMeters = dist, laneX = lane, collected = false))
        }

        startGameLoop()
    }

    // Steering callbacks from UI buttons
    fun setSteerLeft(active: Boolean) {
        steerLeftHeld = active
    }

    fun setSteerRight(active: Boolean) {
        steerRightHeld = active
    }

    fun setAccelerate(active: Boolean) {
        accelerateHeld = active
    }

    fun setBrake(active: Boolean) {
        brakeHeld = active
    }

    fun triggerActiveBoost() {
        if (boostCharge >= 100f && !isBoostActive) {
            isBoostActive = true
            boostCharge = 0f
            boostTimerLeft = 4.5f // 4.5 seconds of super speed!
        }
    }

    // Physics Engine loop
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        val dtSec = 0.016f // 16ms per tick (approx 60fps)

        gameLoopJob = viewModelScope.launch {
            lapStartTimestamp = System.currentTimeMillis()
            var startCounter = 3f // Countdown before race start

            while (isActive) {
                delay(16) // tick rate
                
                if (startCounter > 0f) {
                    startCounter -= dtSec
                    if (startCounter <= 0f) {
                        speedwayAlertText = "GO!"
                    } else {
                        speedwayAlertText = "START IN ${startCounter.toInt() + 1}..."
                    }
                    totalRaceTimeMs = 0L // Keep clock frozen during amber lights
                    continue
                }

                totalRaceTimeMs += 16L
                
                // Track current segment in track
                val totalLength = selectedTrack.lapLengthMeters
                var accum = 0f
                var currentSeg = selectedTrack.segments.first()
                for (seg in selectedTrack.segments) {
                    if (playerProgressMeters >= accum && playerProgressMeters < accum + seg.lengthMeters) {
                        currentSeg = seg
                        break
                    }
                    accum += seg.lengthMeters
                }
                currentSegmentName = currentSeg.name

                // 1. Steering Physics
                val steerInc = dtSec * 1.8f
                if (steerLeftHeld) {
                    playerX = max(-1.2f, playerX - steerInc)
                }
                if (steerRightHeld) {
                    playerX = min(1.2f, playerX + steerInc)
                }

                // 2. Road Bending / Centrifugal Force Offset
                // Road bends, forcing the car left of right.
                // High curving pulls user in opposite direction unless corrected
                val roadPull = currentSeg.curvature * (playerSpeedKmh / 200f) * dtSec * 1.4f
                playerX = (playerX - roadPull).coerceIn(-1.3f, 1.3f)

                // 3. Acceleration & Brake Dynamics
                var maxSpeed = 160f // standard top speed kmh
                var accFactor = 40f  // standard acceleration rate
                
                // Calculate slipstream & drafting opportunities
                val playerTotalDist = (playerLap - 1) * totalLength + playerProgressMeters
                val leadBotsDist = botDistances.filter { it > playerTotalDist }
                val botsAheadCount = leadBotsDist.size
                val nearestAheadBotDist = leadBotsDist.minOrNull() ?: -1f
                
                // Slipstream triggers if you are in a lower spot (at least 4 bots ahead) 
                // OR if you're directly behind ANY bot (within 10 to 90 meters behind)
                val isNearDraft = nearestAheadBotDist > 0f && (nearestAheadBotDist - playerTotalDist) in 10f..90f
                isSlipstreamActive = botsAheadCount >= 4 || isNearDraft

                isOffRoad = abs(playerX) > 1.0f
                if (isOffRoad) {
                    maxSpeed = 80f // off-road sand or gravel drag
                    speedwayAlertText = "⚠️ OFF COURSE - SLOW SPEED!"
                } else {
                    speedwayAlertText = ""
                }

                // Boost integration
                if (isBoostActive) {
                    boostTimerLeft -= dtSec
                    if (boostTimerLeft <= 0f) {
                        isBoostActive = false
                    } else {
                        maxSpeed = 230f // High-top boost
                        accFactor = 120f
                        speedwayAlertText = "🚀 MEGA BOOST INJECTED! 🚀"
                    }
                } else if (isSlipstreamActive && !isOffRoad) {
                    maxSpeed = 195f // Slipstream top speed (normally 160f)
                    accFactor = 70f  // faster acceleration
                    speedwayAlertText = "💨 SLIPSTREAM DRAFTING ACTIVE! +15% MAX SPEED"
                }

                // Apply inputs to speed
                if (accelerateHeld) {
                    if (playerSpeedKmh < maxSpeed) {
                        playerSpeedKmh = min(maxSpeed, playerSpeedKmh + accFactor * dtSec)
                    } else if (playerSpeedKmh > maxSpeed && !isBoostActive) {
                        // Natural engine braking decompression
                        playerSpeedKmh = max(maxSpeed, playerSpeedKmh - 30f * dtSec)
                    }
                } else {
                    // Friction loss
                    playerSpeedKmh = max(0f, playerSpeedKmh - 25f * dtSec)
                }

                if (brakeHeld) {
                    playerSpeedKmh = max(0f, playerSpeedKmh - 120f * dtSec)
                    speedwayAlertText = "⚓ BRAKING"
                }

                // Convert km/h to m/s: Speed / 3.6
                val velocityMps = playerSpeedKmh / 3.6f

                // 4. Update track distance progress
                playerProgressMeters += velocityMps * dtSec

                // 5. Perfect Corner Chain Progression
                // Players gain bonus points/boost when staying exactly close to optimum line on turns
                if (!isOffRoad && abs(currentSeg.curvature) > 0.05f) {
                    val optimumOffset = currentSeg.curvature * 0.5f
                    val deviation = abs(playerX - optimumOffset)
                    if (deviation < 0.35f && playerSpeedKmh > 100f) {
                        perfectCornerStreak++
                        if (perfectCornerStreak % 8 == 0) {
                            boostCharge = min(100f, boostCharge + 2f)
                            totalPerfectCornersInRace++
                            speedwayAlertText = "✨ PERFECT LINE! +2% BOOST"
                        }
                    } else {
                        perfectCornerStreak = 0
                    }
                } else {
                    perfectCornerStreak = 0
                }

                // 6. Check lane boost plate pickups
                val currentMeters = playerProgressMeters
                for (p in boostPickupsOnTrack) {
                    if (!p.collected) {
                        val distanceDiff = abs(p.distanceMeters - currentMeters)
                        if (distanceDiff < 15f) { // close proximity
                            // Check lane overlap: laneX bounds
                            val laneDiff = abs(playerX - p.laneX)
                            if (laneDiff < 0.3f) {
                                p.collected = true
                                boostCharge = min(100f, boostCharge + 25f)
                                playerSpeedKmh = min(220f, playerSpeedKmh + 22f)
                                perfectCornerStreak += 5
                                speedwayAlertText = "⚡ NITRO CHARGE PICKUP!"
                            }
                        }
                    }
                }

                // 7. Simulating real-time AI identical bots (9 opponent bots -> 10 spots)
                for (bIndex in 0..8) {
                    val baseBotSpeed = 44f + (bIndex * 0.8f) // performance diversity
                    val currentCurv = currentSeg.curvature
                    val speedScalar = 1.0f - (abs(currentCurv) * 0.24f) // slow down on corners
                    var botSpeedMps = baseBotSpeed * speedScalar + (Random.nextFloat() * 1.2f - 0.6f)
                    
                    // Rubber-banding (catch-up mechanism)
                    // If this bot is more than 160 meters ahead of the player, slow it down to help player catch up
                    val gap = botDistances[bIndex] - playerTotalDist
                    if (gap > 160f) {
                        botSpeedMps *= 0.81f
                    } else if (gap < -160f) {
                        botSpeedMps *= 1.15f // Speed up to catch up to player
                    }
                    
                    botDistances[bIndex] = botDistances[bIndex] + (botSpeedMps * dtSec)
                }

                // Lap Completion logic
                if (playerProgressMeters >= totalLength) {
                    playerProgressMeters -= totalLength
                    val lapDurationMs = System.currentTimeMillis() - lapStartTimestamp
                    val formattedLap = formatGameDuration(lapDurationMs)
                    lapTimesList.add(formattedLap)
                    
                    lapStartTimestamp = System.currentTimeMillis()
                    playerLap++

                    // Reset track items for next lap so players have new challenges
                    for (p in boostPickupsOnTrack) {
                        p.collected = false
                    }

                    if (playerLap > 3) {
                        playerLap = 3
                        completeActiveRace()
                        break
                    }
                }
            }
        }
    }

    // Wrap-up active race state
    private fun completeActiveRace() {
        gameLoopJob?.cancel()
        
        // Calculate finished total times
        val totalTime = totalRaceTimeMs
        
        // Determine player accumulated total distance
        val playerTotalDist = 3f * selectedTrack.lapLengthMeters

        // Rank the final position relative to bots' total distances
        // Since bots are updated constantly, let's see how they finished:
        var computedPosition = 1
        for (bDist in botDistances) {
            if (bDist > playerTotalDist) {
                computedPosition++
            }
        }
        
        finalPlacement = computedPosition
        
        // Determine ranking point rewards for 10 spots
        pointsChangeByRace = when (computedPosition) {
            1 -> 45
            2 -> 35
            3 -> 25
            4 -> 15
            5 -> 8
            6 -> 2
            7 -> -2
            8 -> -8
            9 -> -15
            else -> -25
        }

        // Setup formatted time outputs
        totalTimeFormatted = formatGameDuration(totalTime)
        
        val bestLapMs = lapTimesList.map { parseFormattedDuration(it) }.minOrNull() ?: (totalTime / 3L)
        currentBestLapFormatted = formatGameDuration(bestLapMs)

        // Save progress to Room Database
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch
            repository.submitRaceResult(
                userId = userId,
                trackName = selectedTrack.name,
                position = computedPosition,
                pointDelta = pointsChangeByRace,
                bestLapTimeMs = bestLapMs,
                totalTimeMs = totalTime,
                perfectCorners = totalPerfectCornersInRace
            )
            
            // Reload user profile reflecting rewards
            val updated = repository.getUserProfileSync(userId)
            _currentUserProfile.value = updated
        }

        screenState = ScreenState.PostRaceSummary
    }

    fun finishRaceSummary() {
        screenState = ScreenState.Dashboard
    }

    // Utility formatting functions
    fun formatGameDuration(ms: Long): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        val hundredths = (ms % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, hundredths)
    }

    private fun parseFormattedDuration(formatted: String): Long {
        return try {
            val parts = formatted.split(":")
            val min = parts[0].toLong()
            val secParts = parts[1].split(".")
            val sec = secParts[0].toLong()
            val hund = secParts[1].toLong()
            
            (min * 60 * 1000) + (sec * 1000) + (hund * 10)
        } catch (e: Exception) {
            0L
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        matchmakingJob?.cancel()
    }
}

data class BoostPickupItem(
    val id: Int,
    val distanceMeters: Float,
    val laneX: Float, // -0.8f (Left), 0.0f (Center), 0.8f (Right)
    var collected: Boolean
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderRankColorHex: String,
    val messageText: String,
    val timestampFormatted: String,
    val isMe: Boolean
)

package com.example.data.repository

import com.example.data.db.UserDao
import com.example.data.db.RaceRecordDao
import com.example.data.db.LeaderboardDao
import com.example.data.model.UserProfile
import com.example.data.model.RaceRecord
import com.example.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class GameRepository(
    private val userDao: UserDao,
    private val raceRecordDao: RaceRecordDao,
    private val leaderboardDao: LeaderboardDao
) {
    fun getUserProfile(userId: String): Flow<UserProfile?> = userDao.getUserById(userId)
    
    suspend fun getUserProfileSync(userId: String): UserProfile? = userDao.getUserByIdSync(userId)

    fun getRecordsForUser(userId: String): Flow<List<RaceRecord>> = 
        raceRecordDao.getRecordsForUser(userId)

    fun getPersonalBest(userId: String, trackName: String): Flow<RaceRecord?> = 
        raceRecordDao.getPersonalBest(userId, trackName)

    val leaderboard: Flow<List<LeaderboardEntry>> = leaderboardDao.getLeaderboard()

    suspend fun insertUser(user: UserProfile) {
        userDao.insertUser(user)
        // Also ensure user is in global leaderboards
        updateLeaderboardUserEntry(user)
    }

    suspend fun updateUser(user: UserProfile) {
        userDao.updateUser(user)
        updateLeaderboardUserEntry(user)
    }

    private suspend fun updateLeaderboardUserEntry(user: UserProfile) {
        leaderboardDao.insertEntry(
            LeaderboardEntry(
                id = user.id,
                username = user.displayName,
                points = user.rankingPoints,
                isUser = true,
                racesCompleted = user.racesCompleted,
                countryCode = "US"
            )
        )
    }

    suspend fun submitRaceResult(
        userId: String,
        trackName: String,
        position: Int, // 1, 2, 3, or 4
        pointDelta: Int, // e.g. +40, +15, -10
        bestLapTimeMs: Long,
        totalTimeMs: Long,
        perfectCorners: Int
    ) {
        val user = userDao.getUserByIdSync(userId) ?: return
        
        // Calculate new stats
        val newPoints = max(0, user.rankingPoints + pointDelta)
        val newRacesCompleted = user.racesCompleted + 1
        val newFirsts = user.firstPlaces + if (position == 1) 1 else 0
        val newSeconds = user.secondPlaces + if (position == 2) 1 else 0
        val newThirds = user.thirdPlaces + if (position == 3) 1 else 0
        val newPerfect = user.perfectCornersCount + perfectCorners

        val updatedUser = user.copy(
            rankingPoints = newPoints,
            racesCompleted = newRacesCompleted,
            firstPlaces = newFirsts,
            secondPlaces = newSeconds,
            thirdPlaces = newThirds,
            perfectCornersCount = newPerfect
        )
        
        userDao.updateUser(updatedUser)
        updateLeaderboardUserEntry(updatedUser)

        // Save highscore race record
        raceRecordDao.insertRecord(
            RaceRecord(
                userId = userId,
                trackName = trackName,
                bestLapTimeMs = bestLapTimeMs,
                bestRaceTimeMs = totalTimeMs
            )
        )
    }

    suspend fun seedDefaultLeaderboard() {
        val seededList = listOf(
            LeaderboardEntry("bot_trash_1", "NoobRider", 65, isUser = false, racesCompleted = 5, countryCode = "US"),
            LeaderboardEntry("bot_bronze_1", "DrifterX", 160, isUser = false, racesCompleted = 12, countryCode = "CA"),
            LeaderboardEntry("bot_bronze_2", "Gearbox", 210, isUser = false, racesCompleted = 18, countryCode = "DE"),
            LeaderboardEntry("bot_silver_1", "ApexMaster", 320, isUser = false, racesCompleted = 24, countryCode = "JP"),
            LeaderboardEntry("bot_silver_2", "CornerClipper", 440, isUser = false, racesCompleted = 31, countryCode = "UK"),
            LeaderboardEntry("bot_gold_1", "BoostFiend", 630, isUser = false, racesCompleted = 45, countryCode = "FR"),
            LeaderboardEntry("bot_gold_2", "Oversteer", 710, isUser = false, racesCompleted = 50, countryCode = "AU"),
            LeaderboardEntry("bot_emerald_1", "LowPolyPrism", 940, isUser = false, racesCompleted = 72, countryCode = "BR"),
            LeaderboardEntry("bot_emerald_2", "FlatShaded", 1080, isUser = false, racesCompleted = 85, countryCode = "NL"),
            LeaderboardEntry("bot_diamond_1", "PrecisionPilot", 1380, isUser = false, racesCompleted = 120, countryCode = "KR"),
            LeaderboardEntry("bot_diamond_2", "VectorViper", 1550, isUser = false, racesCompleted = 145, countryCode = "SE"),
            LeaderboardEntry("bot_king_1", "PolyMonarch", 1880, isUser = false, racesCompleted = 250, countryCode = "CA"),
            LeaderboardEntry("bot_king_2", "Antigravity", 2250, isUser = false, racesCompleted = 380, countryCode = "GB")
        )
        leaderboardDao.insertEntries(seededList)
    }
}

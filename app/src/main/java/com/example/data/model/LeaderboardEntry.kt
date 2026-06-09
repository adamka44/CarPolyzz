package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboards")
data class LeaderboardEntry(
    @PrimaryKey val id: String,
    val username: String,
    val points: Int,
    val isUser: Boolean = false,
    val racesCompleted: Int = 10,
    val countryCode: String = "US"
) {
    fun getRankTier(): RankTier = RankTier.fromPoints(points)
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String, // Google Account ID or Email
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val rankingPoints: Int = 0,
    val racesCompleted: Int = 0,
    val firstPlaces: Int = 0,
    val secondPlaces: Int = 0,
    val thirdPlaces: Int = 0,
    val perfectCornersCount: Int = 0
) {
    fun getRankTier(): RankTier = RankTier.fromPoints(rankingPoints)
}

enum class RankTier(val displayName: String, val minPoints: Int, val colorHex: String) {
    TRASH("Trash", 0, "#8C9BAB"),
    BRONZE("Bronze", 100, "#CD7F32"),
    SILVER("Silver", 250, "#C0C0C0"),
    GOLD("Gold", 500, "#FFD700"),
    EMERALD("Emerald", 800, "#50C878"),
    DIAMOND("Diamond", 1200, "#B9F2FF"),
    KING("King", 1700, "#E63946");

    companion object {
        fun fromPoints(points: Int): RankTier {
            return entries.lastOrNull { points >= it.minPoints } ?: TRASH
        }
        fun getNextTier(current: RankTier): RankTier? {
            val ord = current.ordinal
            return if (ord < entries.size - 1) entries[ord + 1] else null
        }
    }
}

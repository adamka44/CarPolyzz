package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboards ORDER BY points DESC, username ASC")
    fun getLeaderboard(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<LeaderboardEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LeaderboardEntry)

    @Query("DELETE FROM leaderboards WHERE id = :id")
    suspend fun deleteEntry(id: String)
}

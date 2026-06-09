package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RaceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceRecordDao {
    @Query("SELECT * FROM race_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getRecordsForUser(userId: String): Flow<List<RaceRecord>>

    @Query("SELECT * FROM race_records WHERE userId = :userId AND trackName = :trackName ORDER BY bestRaceTimeMs ASC LIMIT 1")
    fun getPersonalBest(userId: String, trackName: String): Flow<RaceRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RaceRecord)
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "race_records")
data class RaceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val trackName: String,
    val bestLapTimeMs: Long,
    val bestRaceTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

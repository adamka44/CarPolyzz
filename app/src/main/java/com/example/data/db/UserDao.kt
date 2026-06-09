package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Update
    suspend fun updateUser(user: UserProfile)
}

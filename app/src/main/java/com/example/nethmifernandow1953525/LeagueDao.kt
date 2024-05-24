package com.example.nethmifernandow1953525

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LeagueDao {

    @Query("SELECT * FROM leagues")
    suspend fun getAll(): List<League>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(leagues: List<League>)

    @Delete
    suspend fun deleteRow(leagues: List<League>)
}
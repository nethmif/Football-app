package com.example.nethmifernandow1953525

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClubsDao {

    @Query("SELECT * FROM clubs")
    suspend fun getAll(): List<Clubs>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clubs: List<Clubs>)

    @Delete
    suspend fun deleteRow(clubs: List<Clubs>)
}
package com.example.nethmifernandow1953525

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Clubs::class], version = 1)
abstract class AppClubDatabase : RoomDatabase() {
    abstract fun clubsDao(): ClubsDao
}

package com.example.nethmifernandow1953525

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clubs")
data class Clubs(
    @PrimaryKey val idLeague: String,
    val strLeague: String,
    val strSport: String,
    val strLeagueAlternate: String,
    val teamID2: String,
    val teamName2: String,
    val teamShort2: String,
    val teamYear2: String,
    val teamStadium2: String,
    val teamKeyword2: String,
    val teamStadiumThumb2: String,
    val teamStadiumLocation2: String,
    val teamStadiumCapacity2: String,
    val teamWebsite2: String,
    val teamJersey2: String,
    val teamLogo2: String
)
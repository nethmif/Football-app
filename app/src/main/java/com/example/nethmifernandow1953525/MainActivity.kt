package com.example.nethmifernandow1953525

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Room
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

lateinit var db: AppDatabase
lateinit var leagueDao: LeagueDao

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(this, AppDatabase::class.java,"mydatabase").build()
        leagueDao = db.leagueDao()
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                val scope = rememberCoroutineScope()
                // Add leagues to database button
                Button(
                    onClick = {
                        scope.launch {
                            insertLeagues(leagueDao)
                            Toast.makeText(this@MainActivity, "Details of the football leagues are saved in the SQLite Database", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .height(70.dp)
                        .width(325.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 25, green = 25, blue = 112)
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(text = "Add Leagues to DB", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.height(50.dp))
                // Search for clubs by league button
                Button(
                    onClick = {
                        val intent = Intent(this@MainActivity, SearchForClubsByLeague::class.java)
                        startActivity(intent)
                    },
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .height(70.dp)
                        .width(325.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 25, green = 25, blue = 112)
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(text = "Search for Clubs By League", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.height(50.dp))
                // Search for clubs button
                Button(
                    onClick = {
                        val intent = Intent(this@MainActivity, SearchForClubs::class.java)
                        startActivity(intent)
                    },
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .height(70.dp)
                        .width(325.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 25, green = 25, blue = 112)
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(text = "Search for Clubs", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.height(50.dp))
                // Jerseys along with their seasons for the corresponding club entered
                var keyword by rememberSaveable { mutableStateOf("") }
                var showSearchField by rememberSaveable { mutableStateOf(false) }
                var showsJersey by rememberSaveable { mutableStateOf(false) }
                if (showSearchField) {
                    // TextField to enter the club name
                    TextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        label = { Text("Enter search string") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                    // After the button click the corresponding jerseys along with the season is displayed
                    Button(
                        onClick = {
                            scope.launch {
                              showsJersey = true
                            }
                        },
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .height(70.dp)
                            .width(325.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(red = 25, green = 25, blue = 112)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(text = "Search", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                    if (showsJersey) {
                        displayJersey(word = keyword)
                    }
                }
                else {
                    // The button is clicked for the Search and the club name TextField to be appeared
                    Button(
                        onClick = { showSearchField = true },
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .height(70.dp)
                            .width(325.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(red = 25, green = 25, blue = 112)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(text = "Enter Club Name", fontSize = 22.sp)
                    }
                }
        }
    }
}
    // Leagues information entered to the database
suspend fun insertLeagues(leagueDao: LeagueDao) {
    val leagues = listOf(
        League("4328", "English Premier League", "Soccer", "Premier League, EPL"),
        League("4329", "English League Championship", "Soccer", "Championship"),
        League("4330", "Scottish Premier League", "Soccer", "Scottish Premiership, SPFL"),
        League("4331", "German Bundesliga", "Soccer", "Bundesliga, Fußball-Bundesliga"),
        League("4332", "Italian Serie A", "Soccer", "Serie A"),
        League("4334", "French Ligue 1", "Soccer", "Ligue 1 Conforama"),
        League("4335", "Spanish La Liga", "Soccer", "LaLiga Santander, La Liga"),
        League("4336", "Greek Superleague Greece", "Soccer", " "),
        League("4337", "Dutch Eredivisie", "Soccer", "Eredivisie"),
        League("4338", "Belgian First Division A", "Soccer", "Jupiler Pro League"),
        League("4339", "Turkish Super Lig", "Soccer", "Super Lig"),
        League("4340", "Danish Superliga", "Soccer", ""),
        League("4344", "Portuguese Primeira Liga", "Soccer", "Liga NOS"),
        League("4346", "American Major League Soccer", "Soccer", "MLS, Major League Soccer"),
        League("4347", "Swedish Allsvenskan", "Soccer", "Fotbollsallsvenskan"),
        League("4350", "Mexican Primera League", "Soccer", "Liga MX"),
        League("4351", "Brazilian Serie A", "Soccer", ""),
        League("4354", "Ukrainian Premier League", "Soccer", ""),
        League("4355", "Russian Football Premier League", "Soccer", "Чемпионат России по футболу"),
        League("4356", "Australian A-League", "Soccer", "A-League"),
        League("4358", "Norwegian Eliteserien", "Soccer", "Eliteserien"),
        League("4359", "Chinese Super League", "Soccer", "")
    )
    leagueDao.insertAll(leagues)
}
}
// All the league ID's are fetched
suspend fun fetchClubInfoLeaguesId(): MutableList<String> {
    val urlString = "https://www.thesportsdb.com/api/v1/json/3/all_leagues.php"
    val url = URL(urlString)
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
    var stb = StringBuilder()
    withContext(Dispatchers.IO) {
        var bf = BufferedReader(InputStreamReader(con.inputStream))
        var line: String? = bf.readLine()
        while (line != null) {
            stb.append(line + "\n")
            line = bf.readLine()
        }
    }
    val allLeagues = parseJSONLeaguesId(stb)
    return allLeagues
}

fun parseJSONLeaguesId(stb: StringBuilder): MutableList<String>{
    val json = JSONObject(stb.toString())
    var allClubs = StringBuilder()
    val leagueIds = mutableListOf<String>()
    var jsonArray: JSONArray = json.getJSONArray("leagues")
    for (i in 0..jsonArray.length()-1){
        try {
            val teamInfo: JSONObject = jsonArray[i] as JSONObject
            val leagueId = teamInfo["idLeague"] as String
            leagueIds.add(leagueId)
        } catch (jen: JSONException) {
        }
    }
    return leagueIds
}
// All the team names and the ID's of the corresponding substring entered in the TextField is extracted
suspend fun fetchClubLeagueInfo(word: String): MutableList<String> {
    var allLeagueId = fetchClubInfoLeaguesId()
    val filteredTeamIds = mutableListOf<String>()
    for (leagueId in allLeagueId){
        val urlString = "https://www.thesportsdb.com/api/v1/json/3/lookuptable.php?l=$leagueId&s=2020-2021"
        try {
            val url = URL(urlString)
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            var stb = StringBuilder()
            withContext(Dispatchers.IO) {
                var bf = BufferedReader(InputStreamReader(con.inputStream))
                var line: String? = bf.readLine()
                while (line != null) {
                    stb.append(line + "\n")
                    line = bf.readLine()
                }
            }
            var allLeagues = parseJSOLeagueInfo(stb)
            for ((teamId, teamName) in allLeagues) {
                if (teamName.contains(word, ignoreCase = true)) {
                    filteredTeamIds.add(teamId)
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
    return filteredTeamIds
}
fun parseJSOLeagueInfo(stb: StringBuilder): MutableList<Pair<String, String>>{
    val json = JSONObject(stb.toString())
    var allClubs = StringBuilder()
    var jsonArray: JSONArray = json.getJSONArray("table")
    val teamInfoList = mutableListOf<Pair<String, String>>()
    for (i in 0..jsonArray.length()-1){
        try {
            val teamInfo: JSONObject = jsonArray[i] as JSONObject
            val teamID = teamInfo.getString("idTeam")
            val teamName = teamInfo.getString("strTeam")
            teamInfoList.add(Pair(teamID, teamName))
        } catch (jen: JSONException) {
        }
    }
    return teamInfoList
}
// Using the team Id of the entered club name the corresponding jerseys for all the seasons are fetched
suspend fun fetchClubJersey(word: String): MutableList<Pair<String, String>>{
    var allClubsId = fetchClubLeagueInfo(word)
    val jerseysSeasons = mutableListOf<Pair<String, String>>()
    for (clubId in allClubsId){
        val urlString = "https://www.thesportsdb.com/api/v1/json/3/lookupequipment.php?id=$clubId"
        try {
            val url = URL(urlString)
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            var stb = StringBuilder()
            withContext(Dispatchers.IO) {
                var bf = BufferedReader(InputStreamReader(con.inputStream))
                var line: String? = bf.readLine()
                while (line != null) {
                    stb.append(line + "\n")
                    line = bf.readLine()
                }
            }
            var allClubJersey = parseJSONClubJersey(stb)
            for ((jerseySeason, jerseyEquipment) in allClubJersey) {
                jerseysSeasons.add(Pair(jerseySeason, jerseyEquipment))
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
    return jerseysSeasons
}
fun parseJSONClubJersey(stb: StringBuilder): MutableList<Pair<String, String>>{
    val json = JSONObject(stb.toString())
    var allClubs = StringBuilder()
    var jsonArray: JSONArray = json.getJSONArray("equipment")
    val clubJerseySeason = mutableListOf<Pair<String, String>>()
    for (i in 0..jsonArray.length()-1){
        try {
            val teamInfo: JSONObject = jsonArray[i] as JSONObject
            val jerseySeason = teamInfo["strSeason"] as String
            val jerseyEquipment = teamInfo["strEquipment"] as String
            clubJerseySeason.add(Pair(jerseySeason, jerseyEquipment))
        } catch (jen: JSONException) {
        }
    }
    return clubJerseySeason
}
// The jersey and the corresponding season is displayed according to the entered club name
@Composable
fun displayJersey(word: String) {
    val logoUrls = remember { mutableStateListOf<String>() }
    val seasonsList = rememberSaveable { mutableListOf<String>() }
    val jerseyUrlsList = rememberSaveable { mutableListOf<String>() }
    LaunchedEffect(word) {
        val clubTestData = fetchClubJersey(word)
        seasonsList.clear()
        jerseyUrlsList.clear()
        clubTestData.forEach { (season, jerseyDetails) ->
            seasonsList.add(season)
            jerseyUrlsList.add(jerseyDetails)
        }
        logoUrls.addAll(jerseyUrlsList)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        logoUrls.forEachIndexed{ index, logoUrl ->
            Text(text = seasonsList[index], fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(15.dp))
            AsyncImage(
                model = logoUrl,
                contentDescription = seasonsList[index]
            )
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
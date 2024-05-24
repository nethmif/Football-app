package com.example.nethmifernandow1953525

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

lateinit var dbClubs: AppClubDatabase
lateinit var clubsDao: ClubsDao

class SearchForClubsByLeague : ComponentActivity() {
    lateinit var prefs: SharedPreferences
    var leagueInfoDisplay = " "
    var leagueSaveDB = " "
    var keyword = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbClubs = Room.databaseBuilder(this, AppClubDatabase::class.java,"clubsdatabase").build()
        // Create an instance of the DAO object
        clubsDao = dbClubs.clubsDao()
        prefs = getSharedPreferences("com.example.nethmifernandow1953525", MODE_PRIVATE)
        leagueInfoDisplay = prefs.getString("leagueInfoDisplay", " ").toString()
        leagueSaveDB = prefs.getString("leagueSaveDB", " ").toString()
        keyword = prefs.getString("keyword", "").toString()
        setContent {
            GUI()
        }
    }
    override fun onPause() {
        super.onPause()
        var editor = prefs.edit()
        editor.putString("leagueInfoDisplay", leagueInfoDisplay)
        editor.putString("leagueSaveDB", leagueSaveDB)
        editor.putString("keyword", keyword)
        editor.apply()
    }
}
@Composable
fun GUI() {
    var clubsRetrieved by rememberSaveable { mutableStateOf(false) }
    var leagueInfoDisplay by rememberSaveable { mutableStateOf(" ") }
    var leagueSaveDB by rememberSaveable { mutableStateOf(" ") }
    var keyword by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent) },
            modifier = Modifier
                .padding(top = 5.dp)
                .height(70.dp)
                .width(325.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(red = 25, green = 25, blue = 112)
            ),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(text = "Home", fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(50.dp))
        TextField(value = keyword, onValueChange = { keyword = it }, label = { Text("Enter the league name") })
        Spacer(modifier = Modifier.height(50.dp))
        // Retrieve clubs button - all the clubs for the corresponding league name entered is displayed
        Button(
            onClick = {
                scope.launch {
                    leagueInfoDisplay = fetchClubInfoRetrieve(keyword)
                    clubsRetrieved = true
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
            Text("Retrieve Clubs", fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        // Save data to the database button also indicating that saving clubs to the database can be done only after retrieving data from the database
        Button(
            onClick = {
                      scope.launch {
                          if (clubsRetrieved) {
                              scope.launch {
                                  leagueSaveDB = fetchClubInfoSave(keyword)
                                  Toast.makeText(context, "Retrieved club details are saved in the database", Toast.LENGTH_SHORT).show()
                              }
                          } else {
                              Toast.makeText(context, "Please retrieve the clubs", Toast.LENGTH_SHORT).show()
                          }
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
            Text(text = "Save Clubs To Database", fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            text = leagueInfoDisplay,
            fontSize = 15.sp
        )
    }
}
// All the league information is retrieved
suspend fun fetchClubInfoRetrieve(league: String): String {
    var urlString = "https://www.thesportsdb.com/api/v1/json/3/search_all_teams.php?l=$league"
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
    val allLeagues = parseJSONRetrieve(stb)
    return allLeagues
}

fun parseJSONRetrieve(stb: StringBuilder): String{
    val json = JSONObject(stb.toString())
    var allClubs = StringBuilder()
    var jsonArray: JSONArray = json.getJSONArray("teams")
    for (i in 0..jsonArray.length()-1){
        try {
            val teamInfo: JSONObject = jsonArray[i] as JSONObject
            val teamID = teamInfo["idTeam"] as String
            val teamName = teamInfo["strTeam"] as String
            val teamShort = teamInfo["strTeamShort"] as String
            val teamAlternate = teamInfo["strAlternate"] as String
            val teamYear = teamInfo["intFormedYear"] as String
            val teamLeague = teamInfo["strLeague"] as String
            val teamLeagueID = teamInfo["idLeague"] as String
            val teamStadium = teamInfo["strStadium"] as String
            val teamKeyword = teamInfo["strKeywords"] as String
            val teamStadiumThumb = teamInfo["strStadiumThumb"] as String
            val teamStadiumLocation = teamInfo["strStadiumLocation"] as String
            val teamStadiumCapacity = teamInfo["intStadiumCapacity"] as String
            val teamWebsite = teamInfo["strWebsite"] as String
            allClubs.append("\"idTeam\":\"$teamID\",\n\"Name\":\"$teamName\",\n\"strTeamShort\":\"$teamShort\",\n\"strAlternate\":\"$teamAlternate\",\n\"intFormedYear\":\"$teamYear\",\n\"strLeague\":\"$teamLeague\",\n\"idLeague\":\"$teamLeagueID\",\n\"strStadium\":\"$teamStadium\",\n\"strKeywords\":\"$teamKeyword\",\n\"strStadiumThumb\":\"$teamStadiumThumb\",\n\"strStadiumLocation\":\"$teamStadiumLocation\",\n\"strStadiumCapacity\":\"$teamStadiumCapacity\",\n\"strWebsite\":\"$teamWebsite\",\n")
                val teamJersey = teamInfo["strTeamJersey"] as String
                val teamLogo = teamInfo["strTeamLogo"] as String
                allClubs.append("\"strTeamJersey\":\"$teamJersey\",\n\"strTeamLogo\":\"$teamLogo\",\n")
        } catch (jen: JSONException) {
        }
        allClubs.append("\n\n")
    }
    return allClubs.toString()
}
// Saving all the retrieved details to the database
suspend fun fetchClubInfoSave(league: String): String {
    var urlString = "https://www.thesportsdb.com/api/v1/json/3/search_all_teams.php?l=$league"
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
    val allLeagues = parseJSONSave(stb, clubsDao)
    return allLeagues
}

suspend fun parseJSONSave(stb: StringBuilder, clubsDao: ClubsDao): String{
    val clubsList = mutableListOf<Clubs>()
    val json = JSONObject(stb.toString())
    var allClubs = StringBuilder()
    var jsonArray: JSONArray = json.getJSONArray("teams")
    for (i in 0..jsonArray.length()-1){
        try {
            val teamInfo: JSONObject = jsonArray[i] as JSONObject
            val club = Clubs(
                idLeague = teamInfo["idLeague"] as String,
                strLeague = teamInfo["strLeague"] as String,
                strSport = "Soccer",
                strLeagueAlternate = teamInfo["strAlternate"] as String,
                teamID2 = teamInfo["idTeam"] as String,
                teamName2 = teamInfo["strTeam"] as String,
                teamShort2 = teamInfo["strTeamShort"] as String,
                teamYear2 = teamInfo["intFormedYear"] as String,
                teamStadium2 = teamInfo["strStadium"] as String,
                teamKeyword2 = teamInfo["strKeywords"] as String,
                teamStadiumThumb2 = teamInfo["strStadiumThumb"] as String,
                teamStadiumLocation2 = teamInfo["strStadiumLocation"] as String,
                teamStadiumCapacity2 = teamInfo["intStadiumCapacity"] as String,
                teamWebsite2 = teamInfo["strWebsite"] as String,
                teamJersey2 = teamInfo["strTeamJersey"] as String,
                teamLogo2 = teamInfo["strTeamLogo"] as String
            )
            clubsList.add(club)
            clubsDao.insertAll(clubsList)
        } catch (jen: JSONException) {
        }
        allClubs.append("\n\n")
    }
    return allClubs.toString()
}
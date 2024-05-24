package com.example.nethmifernandow1953525

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.nethmifernandow1953525.ui.theme.NethmiFernandoW1953525Theme
import kotlinx.coroutines.launch

class SearchForClubs : ComponentActivity() {
    lateinit var prefs: SharedPreferences
    var retrievedInfo = " "
    var keyword = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("com.example.nethmifernandow1953525", MODE_PRIVATE)
        retrievedInfo = prefs.getString("retrievedInfo", " ").toString()
        keyword = prefs.getString("keyword", "").toString()
        setContent {
            searchClubsGUI()
        }
    }
    override fun onPause() {
        super.onPause()
        var editor = prefs.edit()
        editor.putString("retrievedInfo", retrievedInfo)
        editor.putString("keyword", keyword)
        editor.apply()
    }
}
@Composable
fun searchClubsGUI(){
    var showLogos by rememberSaveable { mutableStateOf(false) }
    var retrievedInfo by rememberSaveable { mutableStateOf(" ") }
    val scope = rememberCoroutineScope()
    var context = LocalContext.current
    var keyword by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
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
            // Club name is entered
            TextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Enter the league/ club name") }
        )
            Spacer(modifier = Modifier.height(50.dp))
            // Club details and their logos of the corresponding club name entered will be displayed
            Button(
                onClick = {
                    scope.launch {
                        retrievedInfo = retrieveData(keyword, clubsDao)
                        showLogos = true
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
                Text("Search", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(50.dp))
            if (showLogos) {
                displayClubLogos(keyword, clubsDao)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
        Text(text = retrievedInfo)
    }
}
// Club details
suspend fun retrieveData(word: String, clubsDao: ClubsDao): String {
    var resultsLeague = ""
    val soccer: List<Clubs> = clubsDao.getAll()
    val filteredLeagues = soccer.filter { league ->
        league.strLeague.contains(word, ignoreCase = true) || league.teamName2.contains(word, ignoreCase = true)
    }
    for (p in filteredLeagues) {
        resultsLeague += "League Name: ${p.strLeague},\nLeague Id : ${p.idLeague}, \nLeague Alternate : ${p.strLeagueAlternate}, \nTeam Name : ${p.teamName2}, \nTeam Id : ${p.teamID2}\n\n"
    }
    return resultsLeague
}
// Url of the club logo
suspend fun retrieveLogoUrls(word: String, clubsDao: ClubsDao): List<String> {
    val soccer: List<Clubs> = clubsDao.getAll()
    return soccer.filter {
        it.strLeague.contains(word, ignoreCase = true) || it.teamName2.contains(word, ignoreCase = true)
    }.map { it.teamLogo2 }
}
// Club logo is displayed
@Composable
fun displayClubLogos(word: String, clubsDao: ClubsDao) {
    val logoUrls = remember { mutableStateListOf<String>() }
    LaunchedEffect(word) {
        logoUrls.clear()
        logoUrls.addAll(retrieveLogoUrls(word, clubsDao))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween) {
        logoUrls.forEach { logoUrl ->
            AsyncImage(
                model = logoUrl,
                contentDescription = "Club Logo"
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
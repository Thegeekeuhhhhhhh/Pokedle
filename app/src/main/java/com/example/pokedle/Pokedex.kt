package com.example.pokedle

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W900
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pokedle.ui.theme.PokedleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class Pokedex : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokedleTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(R.drawable.poke_bg),
                        contentDescription = "Poke background",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize(),
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(all = 20.dp).background(Color.Gray)
                ) {
                    Column(
                        modifier = Modifier.matchParentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        GetHeaderText()
                    }

                    Column(
                        modifier = Modifier.matchParentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Loading()
                    }
                }
            }
        }
    }
}

@Composable
fun GetHeaderText() {
    AndroidView(
        factory = {context ->
            val view = LayoutInflater.from(context).inflate(R.layout.pokedex_header, null, false)
            view
        }
    )
}

@Composable
fun Loading() {
    var showLoading by remember { mutableStateOf(true) }
    var pokemonData by remember { mutableStateOf<String>("Nothing") }

    LaunchedEffect(true) {
        // Delay = Simulation
        // Replace delay by fetching
        delay(2000)
        // Print values

        try {
            pokemonData = fetchPokemonData()
        } catch (e: Exception) {
            println(e)
            pokemonData = "Failed to fetch data"
        }
        showLoading = false


    }

    if (showLoading) {
        Text(
            text = "Loading...",
            color = Color.White,
            fontWeight = W900,
            fontSize = 40.sp
        )
    } else {
        Text(
            text = pokemonData,
            color = Color.White,
            fontWeight = W900,
            fontSize = 40.sp
        )
    }
}

data class PokemonResponse(
    val count: Int,
    val results: List<Pokemon>
)

data class Pokemon(
    val name: String,
    val url: String
)


suspend fun fetchPokemonData(): String {
    return withContext(Dispatchers.IO) {
        val url = URL("https://pokeapi.co/api/v2/pokemon/?offset=0")
        val connection = url.openConnection() as HttpsURLConnection
        try {
            val inputStream = BufferedInputStream(connection.inputStream)
            inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }
}
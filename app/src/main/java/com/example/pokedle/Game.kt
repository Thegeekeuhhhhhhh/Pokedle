package com.example.pokedle

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W900
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.pokedle.ui.theme.PokedleTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream

class Game : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        enableEdgeToEdge()
        setContent {
            PokedleTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Image(
                            painter = painterResource(R.drawable.game_bg),
                            contentDescription = "Poke background",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize(),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.poke_bg_landscape),
                            contentDescription = "Poke background",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize(),
                        )
                    }

                    GetHomeButton(start = {
                        val intent = Intent(this@Game, MainActivity::class.java)
                        startActivity(intent)
                    })
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        PokedleGame(resources.openRawResource(R.raw.data))
                    }
                }
            }
        }
    }
}

data class pokemon (
    val name: String,
    val data: pokeData,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class pokeData (
    val french_name :String,
    val type1: String,
    val type2: String?,
    val habitat: String?,
    val color: String,
    val evolution_stage: Int,
    val height: Int,
    val weight: Int,
    val image_link: String,
)

@Composable
fun PokedleGame(content: InputStream) {
    var guess by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    val targetPokemon =
        remember { listOf("pikachu", "bulbasaur", "charmander", "squirtle").random() }

    val tried = remember { mutableMapOf<String, String>() }

    var changedResearch by remember { mutableStateOf(true) }
    var changedGuess by remember { mutableStateOf(true) }

    val jsonstr = content.bufferedReader().readText()
    val pokelist = remember {
        mutableStateOf<Map<String, pokeData>>(
            Json.decodeFromString<Map<String, pokeData>>(jsonstr)
        )
    }
    var toDisplay = remember { mutableStateListOf<Pair<String, String>>() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(top = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Guess the Pokémon!", fontSize = 28.sp, fontWeight = W900)

            OutlinedTextField(
                value = guess,
                onValueChange = {
                    guess = it
                    changedResearch = true
                },
                label = { Text("Enter name") }
            )

            Text(text = feedback, fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp))
        }

        if (changedResearch) {
            if (guess.length == 0) {
                toDisplay.clear()
            } else {
                toDisplay.clear()
                for (elt in pokelist.value.keys) {
                    if (elt.lowercase()
                            .startsWith(guess.lowercase()) && !tried.contains(elt)
                    ) {
                        toDisplay.add(
                            Pair(
                                elt,
                                pokelist.value[elt]?.image_link ?: "CACA LINK"
                            )
                        )
                    }
                }
            }
            changedResearch = false
        }

        val state = rememberScrollState(0)
        if (toDisplay.size > 0) {
            Column(
                modifier = Modifier
                    .background(Color.Transparent).padding(20.dp).height(250.dp)
                    .verticalColumnScrollbar(state)
                    .verticalScroll(
                        state
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0..toDisplay.size - 1) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = {
                            guess = toDisplay[i].first
                            feedback = if (guess.lowercase() == targetPokemon) {
                                "Correct! It was $targetPokemon."
                            } else {
                                "Wrong! Try again."
                            }
                            guess = ""
                            tried.put(toDisplay[i].first, toDisplay[i].second)
                            changedResearch = true
                            changedGuess = true
                        }) {
                            AsyncImage(
                                model = toDisplay[i].second,
                                placeholder = painterResource(R.drawable.loading),
                                error = painterResource(R.drawable.poke_bg),
                                contentDescription = "Image of " + toDisplay[i].first,
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                            )
                            Text(
                                text = toDisplay[i].first.replaceFirstChar {
                                    if (it.isLowerCase()) {
                                        it.titlecase().toString()
                                    } else {
                                        it.toString()
                                    }
                                },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.W700,
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .background(Color.Transparent).height(250.dp)
            ) {
                // Juste histoire de garder de la coherence
            }
        }





        val state2 = rememberScrollState(0)
        if (tried.size > 0) {
            Column(
                modifier = Modifier
                    .background(Color.Transparent).padding(50.dp).height(400.dp)
                    .verticalColumnScrollbar(state2)
                    .verticalScroll(
                        state2
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in tried.keys.reversed()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = tried[i],
                            placeholder = painterResource(R.drawable.loading),
                            error = painterResource(R.drawable.poke_bg),
                            contentDescription = "Image of " + i,
                            modifier = Modifier
                                .height(50.dp)
                                .width(50.dp)
                        )
                        Text(
                            text = i.replaceFirstChar {
                                if (it.isLowerCase()) {
                                    it.titlecase().toString()
                                } else {
                                    it.toString()
                                }
                            },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.W700,
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .background(Color.Transparent).height(250.dp)
            ) {
                // Juste histoire de garder de la coherence
            }
        }








    }
}

@Composable
fun GetHomeButton(start: () -> Unit) {
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.home_button, null, false)
            val button = view.findViewById<ImageButton>(R.id.homeButton)
            button.setOnClickListener { start() }
            view
        }
    )
}
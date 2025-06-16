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
    val jsonstr = content.bufferedReader().readText()

    val pokelistMap = remember {
        Json.decodeFromString<Map<String, pokeData>>(jsonstr)
    }
    val pokelist = remember { mutableStateOf(pokelistMap) }

    val guessData = pokelist.value[guess.lowercase()]

    val targetName = remember { pokelistMap.keys.random() }
    val targetData = pokelistMap[targetName]

    val tried = remember { mutableMapOf<String, String>() }
    var changedResearch by remember { mutableStateOf(true) }

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

            Text(
                text = "🔍 Target (debug): ${targetName.replaceFirstChar { it.uppercase() }}",
                fontSize = 18.sp,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )

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
            if (guess.isEmpty()) {
                toDisplay.clear()
            } else {
                toDisplay.clear()
                for (elt in pokelist.value.keys) {
                    if (elt.lowercase().startsWith(guess.lowercase()) && !tried.contains(elt)) {
                        toDisplay.add(
                            Pair(
                                elt,
                                pokelist.value[elt]?.image_link ?: ""
                            )
                        )
                    }
                }
            }
            changedResearch = false
        }

        val state = rememberScrollState(0)
        if (toDisplay.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .background(Color.Transparent)
                    .padding(20.dp)
                    .height(250.dp)
                    .verticalScroll(state),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in toDisplay.indices) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = {
                            val chosen = toDisplay[i].first
                            guess = chosen
                            val guessData = pokelistMap[guess.lowercase()]
                            if (guess.lowercase() == targetName.lowercase()) {
                                feedback = "✅ Correct! It was ${targetName.replaceFirstChar { it.uppercase() }}.\n"
                                feedback += """
                                    - Type: ${targetData?.type1}${if (targetData?.type2 != null) "/${targetData.type2}" else ""}
                                    - Color: ${targetData?.color}
                                    - Height: ${targetData?.height}
                                    - Weight: ${targetData?.weight}
                                    - Stage: ${targetData?.evolution_stage}
                                """.trimIndent()
                            } else if (guessData != null) {
                                feedback = "❌ Wrong! Try again.\n"
                                feedback += "- Type: ${guessData.type1}" +
                                        (if (guessData.type2 != null) "/${guessData.type2}" else "") +
                                        if ((guessData.type1 == targetData?.type1 || guessData.type1 == targetData?.type2) ||
                                            (guessData.type2 != null && (guessData.type2 == targetData?.type1 || guessData.type2 == targetData?.type2))) {
                                            " ✅"
                                        } else {
                                            ""
                                        }
                                feedback += "\n- Color: ${guessData.color}" +
                                        if (guessData.color == targetData?.color) " ✅" else ""
                                feedback += "\n- Height: ${guessData.height}" +
                                        when {
                                            guessData.height == targetData?.height -> " ✅"
                                            guessData.height > (targetData?.height ?: 0) -> " 🔽"
                                            else -> " 🔼"
                                        }

                                feedback += "\n- Weight: ${guessData.weight}" +
                                        when {
                                            guessData.weight == targetData?.weight -> " ✅"
                                            guessData.weight > (targetData?.weight ?: 0) -> " 🔽"
                                            else -> " 🔼"
                                        }
                                feedback += "\n- Stage: ${guessData.evolution_stage}" +
                                        if (guessData.evolution_stage == targetData?.evolution_stage) " ✅" else ""
                            }
                            guess = ""
                            tried[chosen] = toDisplay[i].second
                            changedResearch = true
                        }) {
                            AsyncImage(
                                model = toDisplay[i].second,
                                placeholder = painterResource(R.drawable.loading),
                                error = painterResource(R.drawable.poke_bg),
                                contentDescription = "Image of ${toDisplay[i].first}",
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                            )
                            Text(
                                text = toDisplay[i].first.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase() else it.toString()
                                },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.W700,
                            )
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.height(250.dp)) {}
        }

        val state2 = rememberScrollState(0)
        if (tried.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(50.dp)
                    .height(400.dp)
                    .verticalScroll(state2),
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
                            contentDescription = "Image of $i",
                            modifier = Modifier
                                .height(50.dp)
                                .width(50.dp)
                        )
                        Text(
                            text = i.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.W700,
                        )
                    }
                }
            }
        } else {
            Column(modifier = Modifier.height(250.dp)) {}
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
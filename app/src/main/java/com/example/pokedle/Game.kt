package com.example.pokedle

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pokedle.ui.theme.PokedleTheme

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
                        PokedleGame()
                    }
                }
            }
        }
    }
}

@Composable
fun PokedleGame() {
    var guess by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    val targetPokemon = remember { listOf("pikachu", "bulbasaur", "charmander", "squirtle").random() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Guess the Pokémon!", fontSize = 28.sp)

        OutlinedTextField(
            value = guess,
            onValueChange = { guess = it },
            label = { Text("Enter name") }
        )

        Button(onClick = {
            feedback = if (guess.lowercase() == targetPokemon) {
                "Correct! It was $targetPokemon."
            } else {
                "Wrong! Try again."
            }
        }) {
            Text("Submit")
        }

        Text(text = feedback, fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp))
    }
}


@Composable
fun GetHomeButton(start: () -> Unit) {
    AndroidView(
        factory = {context ->
            val view = LayoutInflater.from(context).inflate(R.layout.home_button, null, false)
            val button = view.findViewById<ImageButton>(R.id.homeButton)
            button.setOnClickListener { start() }
            view
        }
    )
}
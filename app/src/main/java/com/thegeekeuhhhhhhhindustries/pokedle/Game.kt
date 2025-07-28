package com.thegeekeuhhhhhhhindustries.pokedle

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W900
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.thegeekeuhhhhhhhindustries.pokedle.ui.theme.PokedleTheme
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
                            painter = painterResource(R.drawable.poke_bg),
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

    val tried = remember { mutableMapOf<String, pokeData>() }
    var changedResearch by remember { mutableStateOf(true) }

    var toDisplay = remember { mutableStateListOf<Pair<String, String>>() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(top = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /*
            Text(
                text = "🔍 Target (debug): ${targetName.replaceFirstChar { it.uppercase() }}",
                fontSize = 18.sp,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
            */

            OutlinedTextField(
                value = guess,
                onValueChange = {
                    guess = it
                    changedResearch = true
                },
                label = { Text("Guess the Pokemon !") }
            )
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
                            /*
                            if (guess.lowercase() == targetName.lowercase()) {
                                feedback =
                                    "✅ Correct! It was ${targetName.replaceFirstChar { it.uppercase() }}.\n"
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
                                            (guessData.type2 != null && (guessData.type2 == targetData?.type1 || guessData.type2 == targetData?.type2))
                                        ) {
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
                            */
                            guess = ""
                            tried[chosen] = guessData as pokeData
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
        }

        val state2 = rememberScrollState(0)
        if (tried.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .height(400.dp)
                    .verticalScroll(state2),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Type1",
                        modifier = Modifier.width(50.dp).fillMaxWidth(),
                        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                    Text(
                        text = "Type2",
                        modifier = Modifier.width(50.dp).fillMaxWidth(),
                        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                    Text(
                        text = "Color",
                        modifier = Modifier.width(50.dp).fillMaxWidth(),
                        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                    Text(
                        text = "Habitat",
                        modifier = Modifier.width(50.dp).fillMaxWidth(),
                        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                    Text(
                        text = "Height",
                        modifier = Modifier.width(50.dp).fillMaxWidth(),
                        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                    Text(
                        text = "Weight",
                        modifier = Modifier.width(50.dp).fillMaxWidth(),
                        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                    Text(
                        text = "Evol. Stage",
                        modifier = Modifier.width(50.dp).fillMaxWidth(),
                        style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    )
                }
                for (i in tried.keys.reversed()) {
                    Box(Modifier.background(Color.White)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    model = tried[i]?.image_link,
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


                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                var col = Color.Red
                                if (tried[i]?.type1 == targetData?.type1) {
                                    col = Color.Green;
                                }
                                Box(
                                    modifier = Modifier.size(50.dp).background(col),
                                    contentAlignment = Alignment.Center
                                ) {
                                    var txt = tried[i]?.type1.toString()
                                    if (txt == "null") {
                                        txt = "None"
                                    }
                                    Text(txt, fontSize = 12.sp);
                                }

                                col = Color.Red
                                if (tried[i]?.type2 == targetData?.type2) {
                                    col = Color.Green;
                                }
                                Box(
                                    modifier = Modifier.size(50.dp).background(col),
                                    contentAlignment = Alignment.Center
                                ) {
                                    var txt = tried[i]?.type2.toString()
                                    if (txt == "null") {
                                        txt = "None"
                                    }
                                    Text(txt, fontSize = 12.sp);
                                }

                                col = Color.Red
                                if (tried[i]?.color == targetData?.color) {
                                    col = Color.Green;
                                }
                                Box(
                                    modifier = Modifier.size(50.dp).background(col),
                                    contentAlignment = Alignment.Center
                                ) {
                                    var txt = tried[i]?.color.toString()
                                    if (txt == "null") {
                                        txt = "None"
                                    }
                                    Text(txt, fontSize = 12.sp);
                                }

                                col = Color.Red
                                if (tried[i]?.habitat == targetData?.habitat) {
                                    col = Color.Green;
                                }
                                Box(
                                    modifier = Modifier.size(50.dp).background(col),
                                    contentAlignment = Alignment.Center
                                ) {
                                    var txt = tried[i]?.habitat.toString()
                                    if (txt == "null") {
                                        txt = "None"
                                    }
                                    Text(txt, fontSize = 12.sp);
                                }

                                col = Color.Yellow
                                if (tried[i]?.height == targetData?.height) {
                                    col = Color.Green;
                                }
                                Log.i("lol", tried[i]?.height.toString())
                                Box(
                                    modifier = Modifier.size(50.dp).background(col),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tried[i]?.height!! > targetData?.height!!) {
                                        Box(
                                            modifier = Modifier.drawWithCache {
                                                val path = Path()
                                                path.moveTo(10f, 40f)
                                                path.lineTo(size.width / 2f, size.height / 2f + 40)
                                                path.lineTo(size.width - 10f, 40f)
                                                path.close()
                                                onDrawBehind {
                                                    drawPath(
                                                        path,
                                                        Color.DarkGray,
                                                        style = Stroke(width = 10f)
                                                    )
                                                }
                                            }.fillMaxSize()
                                        )
                                    } else if (tried[i]?.height!! < targetData?.height!!) {
                                        Box(
                                            modifier = Modifier.drawWithCache {
                                                val path = Path()
                                                path.moveTo(
                                                    10f,
                                                    size.height - 40f
                                                ) // Left base corner
                                                path.lineTo(
                                                    size.width / 2f,
                                                    size.height / 2f - 40f
                                                ) // Tip of arrow (higher up)
                                                path.lineTo(
                                                    size.width - 10f,
                                                    size.height - 40f
                                                ) // Right base corner
                                                path.close()
                                                onDrawBehind {
                                                    drawPath(
                                                        path,
                                                        Color.DarkGray,
                                                        style = Stroke(width = 10f)
                                                    )
                                                }
                                            }.fillMaxSize()
                                        )
                                    }
                                    val h = tried[i]?.height!!.toDouble() / 10
                                    Text(h.toString() + "0m", fontSize = 12.sp);
                                }

                                col = Color.Yellow
                                if (tried[i]?.weight == targetData?.weight) {
                                    col = Color.Green;
                                }
                                Log.i("lol", tried[i]?.weight.toString())
                                Box(
                                    modifier = Modifier.size(50.dp).background(col),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tried[i]?.weight!! > targetData?.weight!!) {
                                        Box(
                                            modifier = Modifier.drawWithCache {
                                                val path = Path()
                                                path.moveTo(10f, 40f)
                                                path.lineTo(size.width / 2f, size.height / 2f + 40)
                                                path.lineTo(size.width - 10f, 40f)
                                                path.close()
                                                onDrawBehind {
                                                    drawPath(
                                                        path,
                                                        Color.DarkGray,
                                                        style = Stroke(width = 10f)
                                                    )
                                                }
                                            }.fillMaxSize()
                                        )
                                    } else if (tried[i]?.weight!! < targetData?.weight!!) {
                                        Box(
                                            modifier = Modifier.drawWithCache {
                                                val path = Path()
                                                path.moveTo(
                                                    10f,
                                                    size.height - 40f
                                                ) // Left base corner
                                                path.lineTo(
                                                    size.width / 2f,
                                                    size.height / 2f - 40f
                                                ) // Tip of arrow (higher up)
                                                path.lineTo(
                                                    size.width - 10f,
                                                    size.height - 40f
                                                ) // Right base corner
                                                path.close()
                                                onDrawBehind {
                                                    drawPath(
                                                        path,
                                                        Color.DarkGray,
                                                        style = Stroke(width = 10f)
                                                    )
                                                }
                                            }.fillMaxSize()
                                        )
                                    }
                                    Text(tried[i]?.weight.toString() + "kg", fontSize = 12.sp);
                                }

                                col = Color.Red
                                if (tried[i]?.evolution_stage == targetData?.evolution_stage) {
                                    col = Color.Green;
                                }
                                Box(
                                    modifier = Modifier.size(50.dp).background(col),
                                    contentAlignment = Alignment.Center
                                ) {
                                    var txt = tried[i]?.evolution_stage.toString()
                                    if (txt == "null") {
                                        txt = "None"
                                    }
                                    Text(txt, fontSize = 12.sp);
                                }
                            }
                        }
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
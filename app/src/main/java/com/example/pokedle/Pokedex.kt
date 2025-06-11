package com.example.pokedle

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W900
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.pokedle.ui.theme.PokedleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

val PokedexDataListSaver = Saver<SnapshotStateList<PokedexData>, List<Map<String, Any>>>(
    save = { list ->
        list.map { pokemon ->
            mapOf(
                "name" to pokemon.name,
                "picUrl" to pokemon.picUrl,
                "id" to pokemon.id
            )
        }
    },
    restore = { savedList ->
        savedList.map { map ->
            PokedexData(
                name = map["name"] as String,
                picUrl = map["picUrl"] as String,
                id = map["id"] as Int
            )
        }.toMutableStateList()
    }
)

class Pokedex : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 20.dp)
                        .background(Color.Gray)
                ) {
                    Column(
                        modifier = Modifier.matchParentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        GetHeaderText()
                    }

                    Column(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(top = 70.dp), // 120 c'est du bluff la team
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Loading(resources.configuration.orientation)
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
fun LoadMoreButton(start: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    AndroidView(
        factory = {context ->
            val view = LayoutInflater.from(context).inflate(R.layout.load_more, null, false)
            val button = view.findViewById<Button>(R.id.loadMoreButton)
            button.setOnClickListener {
                if (loading == false) {
                    loading = true
                    button.isEnabled = false
                    button.isClickable = false
                    button.text = "Loading..."
                    scope.launch {
                        try {
                            start()
                        } catch (e: Exception) {
                            // TODO
                            // Gerer l exception
                        } finally {
                            loading = false
                            button.isEnabled = true
                            button.isClickable = true
                            button.text = "LOAD MORE"
                        }
                    }
                }
            }
            view
        }
    )
}

@Composable
fun Modifier.verticalColumnScrollbar(
    scrollState: ScrollState,
    width: Dp = 4.dp,
    showScrollBarTrack: Boolean = true,
    scrollBarTrackColor: Color = Color.Gray,
    scrollBarColor: Color = Color.Black,
    scrollBarCornerRadius: Float = 4f,
    endPadding: Float = 12f
): Modifier {
    return drawWithContent {
        drawContent()
        val viewportHeight = this.size.height
        val totalContentHeight = scrollState.maxValue.toFloat() + viewportHeight
        val scrollValue = scrollState.value.toFloat()
        val scrollBarHeight =
            (viewportHeight / totalContentHeight) * viewportHeight
        val scrollBarStartOffset =
            (scrollValue / totalContentHeight) * viewportHeight
        if (showScrollBarTrack) {
            drawRoundRect(
                cornerRadius = CornerRadius(scrollBarCornerRadius),
                color = scrollBarTrackColor,
                topLeft = Offset(this.size.width - endPadding, 0f),
                size = Size(width.toPx(), viewportHeight),
            )
        }
        drawRoundRect(
            cornerRadius = CornerRadius(scrollBarCornerRadius),
            color = scrollBarColor,
            topLeft = Offset(this.size.width - endPadding, scrollBarStartOffset),
            size = Size(width.toPx(), scrollBarHeight)
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Loading(orientation: Int) : String {
    var showLoading by remember { mutableStateOf(true) }
    var changedResearch by remember { mutableStateOf(true) }
    var pokemonData by remember { mutableStateOf<String>("Nothing") }
    var index = remember { 1 }
    val pokes = rememberSaveable(saver = PokedexDataListSaver) {
        mutableStateListOf<PokedexData>()
    }
    var pokesToDisplay = remember { mutableStateListOf<PokedexData>() }

    var guess by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        try {
            if (pokes.isEmpty()) {
                pokemonData = fetch("https://pokeapi.co/api/v2/pokemon/?offset=0")
                val json = JSONObject(pokemonData)
                val results = json.getJSONArray("results")

                val threads = (0..19).map { i ->
                    async(Dispatchers.IO) {
                        val apiUrl = results.getJSONObject(i).get("url").toString()
                        val pokemonResult = JSONObject(fetch(apiUrl))
                        val pic =
                            pokemonResult.getJSONObject("sprites").get("front_default").toString()
                        withContext(Dispatchers.Main) {
                            pokes.add(
                                PokedexData(
                                    results.getJSONObject(i).get("name").toString(),
                                    pic,
                                    i,
                                )
                            )
                        }
                    }
                }

                threads.awaitAll()
                pokes.sortWith({ lhs: PokedexData, rhs: PokedexData -> lhs.id - rhs.id })
            }
        } catch (e: Exception) {
            pokemonData = "Fetch fail"
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
        return ""
    } else {
        if (changedResearch) {
            if (guess.length == 0) {
                pokesToDisplay.clear()
                for (i in 0..pokes.size - 1) {
                    pokesToDisplay.add(PokedexData(pokes[i].name, pokes[i].picUrl, pokes[i].id))
                }
            } else {
                pokesToDisplay.clear()
                for (i in 0..pokes.size - 1) {
                    if (pokes[i].name.lowercase().startsWith(guess.lowercase())) {
                        pokesToDisplay.add(PokedexData(pokes[i].name, pokes[i].picUrl, pokes[i].id))
                    }
                }
            }
            changedResearch = false
        }
        val state = rememberScrollState(0)
        Column(
            modifier = Modifier
                .background(Color.Transparent)
                .verticalColumnScrollbar(state)
                .verticalScroll(
                    state
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = guess,
                onValueChange = {
                    guess = it
                    changedResearch = true
                                },
                label = { Text("Research") }
            )
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                for (i in 0..pokesToDisplay.size - 1) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = {
                            println(pokesToDisplay[i].name)
                            // TODO
                            // Redigerer vers la page Pokedex
                        }) {
                            AsyncImage(
                                model = pokesToDisplay[i].picUrl,
                                placeholder = painterResource(R.drawable.loading),
                                error = painterResource(R.drawable.poke_bg),
                                contentDescription = "Image of " + pokesToDisplay[i].name,
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                            )
                            Text(
                                text = pokesToDisplay[i].name.replaceFirstChar {
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
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        for (i in 0..pokesToDisplay.size - 1 step 2) {
                            Button(
                                onClick = {
                                    println(pokesToDisplay[i].name)
                                    // TODO
                                    // Redigerer vers la page Pokedex
                                }) {
                                AsyncImage(
                                    model = pokesToDisplay[i].picUrl,
                                    placeholder = painterResource(R.drawable.loading),
                                    error = painterResource(R.drawable.poke_bg),
                                    contentDescription = "Image of " + pokesToDisplay[i].name,
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(50.dp)
                                )
                                Text(
                                    text = pokesToDisplay[i].name.replaceFirstChar {
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

                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        for (i in 1..pokesToDisplay.size - 1 step 2) {
                            Button(
                                onClick = {
                                    println(pokesToDisplay[i].name)
                                    // TODO
                                    // Redigerer vers la page Pokedex
                                }) {
                                AsyncImage(
                                    model = pokesToDisplay[i].picUrl,
                                    placeholder = painterResource(R.drawable.loading),
                                    error = painterResource(R.drawable.poke_bg),
                                    contentDescription = "Image of " + pokesToDisplay[i].name,
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(50.dp)
                                )
                                Text(
                                    text = pokesToDisplay[i].name.replaceFirstChar {
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
            }

            LoadMoreButton(){
                withContext(Dispatchers.IO) {
                    try {
                        pokemonData =
                            fetch("https://pokeapi.co/api/v2/pokemon/?offset=" + index * 20)
                        index += 1
                        val json = JSONObject(pokemonData)
                        val results = json.getJSONArray("results")

                        val threads = (0..19).map { i ->
                            async(Dispatchers.IO) {
                                val apiUrl = results.getJSONObject(i).get("url").toString()
                                val pokemonResult = JSONObject(fetch(apiUrl))
                                val pic =
                                    pokemonResult.getJSONObject("sprites").get("front_default")
                                        .toString()
                                withContext(Dispatchers.Main) {
                                    pokes.add(
                                        PokedexData(
                                            results.getJSONObject(i).get("name").toString(),
                                            pic,
                                            index*20 + i,
                                        )
                                    )
                                }
                            }
                        }

                        threads.awaitAll()
                        println("REAL SIZE" + pokes.size)
                        pokes.sortWith({ lhs: PokedexData, rhs: PokedexData -> lhs.id - rhs.id })
                        changedResearch = true
                    } catch (e: Exception) {
                        pokemonData = "Fetch fail"
                    }
                }
            }
        }

        return pokemonData
    }
}

@Parcelize
data class PokedexData(
    val name: String,
    val picUrl: String,
    val id: Int,
) : Parcelable

data class PokemonResponse(
    val count: Int,
    val results: List<Pokemon>
)

data class Pokemon(
    val name: String,
    val url: String
)

suspend fun fetch(address: String): String {
    return withContext(Dispatchers.IO) {
        val url = URL(address)
        val connection = url.openConnection() as HttpsURLConnection
        try {
            val inputStream = BufferedInputStream(connection.inputStream)
            inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }
}
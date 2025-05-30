package com.example.pokedle

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pokedle.ui.theme.PokedleTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
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

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        GetButton(start = {
                            val intent = Intent(this@MainActivity, Game::class.java)
                            startActivity(intent)
                        })
                        GetButton(start = {
                            val intent = Intent(this@MainActivity, Pokedex::class.java)
                            startActivity(intent)
                        })
                        BeautifulColors("Dardan Bytyqi\nHugo Viala\nLéo Menaldo")
                    }
                }
            }
        }
    }
}

@Composable
fun BeautifulColors(s: String) {
    val temp = rememberInfiniteTransition()

    val colorList = remember {
        List(100) {
            Color(
                red = Random.nextFloat() * 0.3f + 0.2f,
                green = Random.nextFloat() * 0.8f + 0.2f,
                blue = 0.3f,
                alpha = 1f
            )
        }
    }

    var index1 by remember { mutableIntStateOf(0) }
    var index2 by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            index1 = index2
            index2 = (index1 + 1 + Random.nextInt(0, colorList.size)) % colorList.size
            delay(1000)
        }
    }

    val color by temp.animateColor(
        initialValue = colorList[index1],
        targetValue = colorList[index2],
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Text(
        text = s,
        textAlign = TextAlign.Center,
        lineHeight = 55.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.W900,
        color = color,
        fontSize = 50.sp
    )
}

@Composable
fun GetButton(start: () -> Unit) {
    AndroidView(
        factory = {context ->
            val view = LayoutInflater.from(context).inflate(R.layout.start_button, null, false)
            val button = view.findViewById<Button>(R.id.startButton)
            button.setOnClickListener { start() }
            view
        }
    )
}
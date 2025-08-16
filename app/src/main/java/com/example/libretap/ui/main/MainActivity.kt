package com.example.libretap.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.libretap.services.AutoClickService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BasicModeScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicModeScreen() {
    var repeatCount by remember { mutableStateOf("5") }
    var delayMs by remember { mutableStateOf("300") }
    var lastTap by remember { mutableStateOf<Offset?>(null) }

    val rootView = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Basic Mode", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = repeatCount,
            onValueChange = { txt -> repeatCount = txt.filter { it.isDigit() } },
            label = { Text("Repeat Count") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = delayMs,
            onValueChange = { txt -> delayMs = txt.filter { it.isDigit() } },
            label = { Text("Delay (ms)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Koordinat seçme alanı
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Compose offset'i (view içi) -> ekran koordinatına dönüştür
                        val loc = IntArray(2)
                        rootView.getLocationOnScreen(loc)
                        val screenX = offset.x + loc[0]
                        val screenY = offset.y + loc[1]
                        lastTap = Offset(screenX, screenY)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = lastTap?.let { "Selected: ${it.x.toInt()}, ${it.y.toInt()}" }
                    ?: "Tap anywhere to select a point",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    val pt = lastTap ?: return@Button
                    val times = (repeatCount.toIntOrNull() ?: 1).coerceAtLeast(1)
                    val delay = (delayMs.toLongOrNull() ?: 300L).coerceAtLeast(0L)
                    AutoClickService.instance?.startClicking(pt.x, pt.y, times, delay)
                }
            ) { Text("Start") }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { AutoClickService.instance?.stopClicking() }
            ) { Text("Stop") }
        }
    }
}

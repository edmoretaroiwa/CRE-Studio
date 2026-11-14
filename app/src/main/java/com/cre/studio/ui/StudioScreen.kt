package com.cre.studio.ui

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.concurrent.thread
import kotlin.math.sin

@Composable
fun StudioScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CRE STUDIO",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Master Transport (Play/Stop)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ThreeDButton(text = "PLAY", baseColor = Color(0xFF4CAF50)) {
                playTone(440.0) // A4 Note
            }
            ThreeDButton(text = "STOP", baseColor = Color(0xFFF44336)) {
                // Stop logic
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Drum Pads", color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // 4x2 Pad Grid
        val padColors = listOf(Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFFE91E63))
        val frequencies = listOf(261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25) // C Major Scale

        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..3) {
                    val index = (row * 4) + col
                    ThreeDButton(
                        text = "PAD ${index + 1}",
                        baseColor = padColors[col],
                        modifier = Modifier.size(75.dp),
                        onClick = { playTone(frequencies[index]) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ThreeDButton(
    text: String,
    baseColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp, // High elevation for 3D effect
                shape = RoundedCornerShape(12.dp),
                ambientColor = baseColor,
                spotColor = baseColor
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.6f), // Highlight at top
                        baseColor,                    // Base color
                        baseColor.copy(alpha = 0.8f)  // Shadow at bottom
                    )
                )
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

// Native Audio Synth Engine (No external files needed)
fun playTone(frequency: Double) {
    thread {
        val sampleRate = 44100
        val duration = 0.5 // seconds
        val numSamples = (duration * sampleRate).toInt()
        val generatedSnd = ShortArray(numSamples)
        
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            numSamples * 2,
            AudioTrack.MODE_STATIC
        )

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
            // Apply an envelope to prevent clicking sound
            val envelope = if (i < 2000) i / 2000.0 else if (i > numSamples - 2000) (numSamples - i) / 2000.0 else 1.0
            generatedSnd[i] = (sin(angle) * Short.MAX_VALUE * envelope * 0.5).toInt().toShort()
        }

        audioTrack.write(generatedSnd, 0, numSamples)
        audioTrack.play()
    }
}

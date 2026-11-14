// Add this import at the top of StudioScreen.kt
import com.cre.studio.ui.components.HardwareButton
import androidx.compose.foundation.layout.Arrangement

// ... (Keep your sequencer logic and TimelineGrid)

@Composable
fun TransportBar(isPlaying: Boolean, bpm: Int, onPlayToggle: () -> Unit, onBpmChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between buttons
    ) {
        // Hardware PLAY Button
        HardwareButton(
            label = "PLAY",
            isActive = isPlaying,
            activeColor = PlayLedActive,
            onClick = onPlayToggle
        )

        // Hardware RECORD Button (Just visual for now)
        HardwareButton(
            label = "REC",
            isActive = false, // Change this later when recording logic is added
            activeColor = RecordLedActive,
            onClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.weight(1f))

        // BPM Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("BPM", color = Color.Gray, fontSize = 12.sp)
            Text("$bpm", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

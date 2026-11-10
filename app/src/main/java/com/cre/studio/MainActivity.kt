package com.cre.studio

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.content.res.ColorStateList
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.view.Gravity
import android.widget.*
import android.view.ViewGroup
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Random
import kotlin.math.*

class MainActivity : Activity() {

    private val sampleRate = 44100
    private val tracks = listOf(
        "KICK", "SNARE", "HI-HAT", "CLAP"
    )

    private val pattern = Array(4) {
        BooleanArray(16)
    }

    private val stepButtons =
        Array(4) { arrayOfNulls<Button>(16) }

    private var bpm = 120
    private var isPlaying = false
    private var metronome = true
    private var audioTrack: AudioTrack? = null

    private val dark = Color.rgb(16, 19, 27)
    private val panel = Color.rgb(30, 35, 48)
    private val purple = Color.rgb(155, 100, 255)
    private val muted = Color.rgb(53, 59, 75)

    private lateinit var bpmLabel: TextView
    private lateinit var playButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark

        // Default drum pattern
        pattern[0][0] = true
        pattern[0][8] = true
        pattern[1][4] = true
        pattern[1][12] = true

        for (i in 0 until 16 step 2) {
            pattern[2][i] = true
        }

        pattern[3][12] = true

        buildInterface()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun label(
        value: String,
        size: Float
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
        }
    }

    private fun button(
        value: String,
        color: Int = panel
    ): Button {
        return Button(this).apply {
            text = value
            textSize = 12f
            setTextColor(Color.WHITE)
            backgroundTintList =
                ColorStateList.valueOf(color)
        }
    }

    private fun buildInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(16))
            setBackgroundColor(dark)
        }

        val scroll = ScrollView(this)
        scroll.addView(root)

        val title = label("CRE STUDIO", 27f)
        title.setTextColor(purple)
        title.setTypeface(null, 1)

        root.addView(title)

        root.addView(
            label(
                "CREATIVE RESILIENT EVOLVING",
                10f
            )
        )

        addSpace(root, 20)

        root.addView(
            label("DRUM MACHINE", 19f)
        )

        addSpace(root, 10)

        val bpmRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }

        bpmLabel = label("BPM: $bpm", 17f)
        bpmRow.addView(
            bpmLabel,
            LinearLayout.LayoutParams(
                dp(100), dp(48)
            )
        )

        val tempo = SeekBar(this).apply {
            max = 120
            progress = bpm - 60

            setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        bpm = progress + 60
                        bpmLabel.text = "BPM: $bpm"
                    }

                    override fun onStartTrackingTouch(
                        seekBar: SeekBar?
                    ) {}

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {
                        if (isPlaying) startPlayback()
                    }
                }
            )
        }

        bpmRow.addView(
            tempo,
            LinearLayout.LayoutParams(
                0, dp(48), 1f
            )
        )

        root.addView(bpmRow)

        val metro = CheckBox(this).apply {
            text = "Metronome"
            isChecked = true
            setTextColor(Color.WHITE)

            setOnCheckedChangeListener { _, checked ->
                metronome = checked
                if (isPlaying) startPlayback()
            }
        }

        root.addView(metro)

        addSpace(root, 12)

        root.addView(
            label("16-STEP SEQUENCER", 14f)
        )

        addSpace(root, 8)

        for (track in 0 until 4) {

            val name = label(tracks[track], 13f)
            root.addView(name)

            val horizontal = HorizontalScrollView(this)

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            for (step in 0 until 16) {

                val cell = Button(this).apply {
                    text = "·"
                    textSize = 12f
                    setPadding(0, 0, 0, 0)
                    minWidth = 0
                    minimumWidth = 0
                    minHeight = dp(42)
                    minimumHeight = dp(42)
                    setTextColor(Color.WHITE)

                    backgroundTintList =
                        ColorStateList.valueOf(muted)
                }

                cell.layoutParams =
                    LinearLayout.LayoutParams(
                        dp(38), dp(44)
                    ).apply {
                        setMargins(dp(2), dp(2), dp(2), dp(2))
                    }

                cell.setOnClickListener {

                    pattern[track][step] =
                        !pattern[track][step]

                    refreshGrid()

                    if (isPlaying) startPlayback()
                }

                stepButtons[track][step] = cell
                row.addView(cell)
            }

            horizontal.addView(row)
            root.addView(horizontal)

            addSpace(root, 8)
        }

        refreshGrid()

        addSpace(root, 12)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        playButton = button("PLAY", purple)

        playButton.setOnClickListener {
            startPlayback()
        }

        val stop = button("STOP")

        stop.setOnClickListener {
            stopPlayback()
        }

        controls.addView(
            playButton,
            LinearLayout.LayoutParams(
                0, dp(52), 1f
            ).apply {
                setMargins(0, 0, dp(6), 0)
            }
        )

        controls.addView(
            stop,
            LinearLayout.LayoutParams(
                0, dp(52), 1f
            ).apply {
                setMargins(dp(6), 0, 0, 0)
            }
        )

        root.addView(controls)

        addSpace(root, 12)

        val storage = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val save = button("SAVE PROJECT")
        val load = button("LOAD PROJECT")

        save.setOnClickListener { saveProject() }
        load.setOnClickListener { loadProject() }

        storage.addView(
            save,
            LinearLayout.LayoutParams(0, dp(50), 1f)
        )

        storage.addView(
            load,
            LinearLayout.LayoutParams(0, dp(50), 1f)
        )

        root.addView(storage)

        addSpace(root, 20)

        root.addView(
            label(
                "CRE STUDIO  •  OFFLINE MODE",
                11f
            )
        )

        setContentView(scroll)
    }

    private fun addSpace(
        parent: LinearLayout,
        size: Int
    ) {
        parent.addView(
            android.view.View(this),
            LinearLayout.LayoutParams(
                1, dp(size)
            )
        )
    }

    private fun refreshGrid() {

        for (track in 0 until 4) {
            for (step in 0 until 16) {

                val active = pattern[track][step]
                val cell = stepButtons[track][step]

                cell?.text = if (active) "●" else "·"

                cell?.backgroundTintList =
                    ColorStateList.valueOf(
                        if (active) purple else muted
                    )
            }
        }
    }

    private fun startPlayback() {

        stopAudio()

        val pcm = renderPattern()

        try {
            val format = AudioFormat.Builder()
                .setEncoding(
                    AudioFormat.ENCODING_PCM_16BIT
                )
                .setSampleRate(sampleRate)
                .setChannelMask(
                    AudioFormat.CHANNEL_OUT_MONO
                )
                .build()

            val attributes =
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_MEDIA
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .build()

            val player = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setTransferMode(
                    AudioTrack.MODE_STATIC
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .build()

            if (player.state != AudioTrack.STATE_INITIALIZED) {
                player.release()
                toast("Audio initialization failed")
                return
            }

            val written = player.write(
                pcm, 0, pcm.size,
                AudioTrack.WRITE_BLOCKING
            )

            if (written != pcm.size) {
                player.release()
                toast("Audio buffer write failed")
                return
            }

            val result = player.setLoopPoints(
                0, pcm.size, -1
            )

            if (result != AudioTrack.SUCCESS) {
                player.release()
                toast("Audio looping unavailable")
                return
            }

            audioTrack = player
            player.play()

            isPlaying = true
            playButton.text = "RESTART"

        } catch (error: Exception) {
            stopAudio()
            toast("Playback error: ${error.message}")
        }
    }

    private fun stopAudio() {

        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.pause()
                }
                it.flush()
                it.stop()
                it.release()
            }
        } catch (_: Exception) {
        }

        audioTrack = null
        isPlaying = false

        if (::playButton.isInitialized) {
            playButton.text = "PLAY"
        }
    }

    private fun stopPlayback() {
        stopAudio()
    }

    private fun renderPattern(): ShortArray {

        val framesPerStep = (
            sampleRate * 60.0 / bpm / 4.0
        ).roundToInt()

        val totalFrames = framesPerStep * 16

        val mix = FloatArray(totalFrames)

        for (track in 0 until 4) {

            for (step in 0 until 16) {

                if (pattern[track][step]) {

                    val start =
                        step * framesPerStep

                    renderHit(
                        track,
                        mix,
                        start
                    )
                }
            }
        }

        if (metronome) {
            for (step in 0 until 16 step 4) {
                addMetronome(
                    mix,
                    step * framesPerStep,
                    step == 0
                )
            }
        }

        var peak = 0f

        for (sample in mix) {
            peak = max(peak, abs(sample))
        }

        val gain = 0.85f / max(1f, peak)

        return ShortArray(totalFrames) { index ->

            val sample =
                (mix[index] * gain)
                    .coerceIn(-1f, 1f)

            (sample * 32767f).toInt().toShort()
        }
    }

    private fun renderHit(
        track: Int,
        output: FloatArray,
        start: Int
    ) {

        val duration = when (track) {
            0 -> 0.40
            1 -> 0.22
            2 -> 0.10
            else -> 0.22
        }

        val length = min(
            (duration * sampleRate).toInt(),
            output.size - start
        )

        val random = Random(
            start.toLong() + track * 1000L
        )

        for (i in 0 until length) {

            val t = i.toDouble() / sampleRate

            val noise =
                random.nextDouble() * 2.0 - 1.0

            val value = when (track) {

                // Kick
                0 -> {
                    val phase =
                        2.0 * PI * (
                            48.0 * t +
                            4.5 * (1.0 - exp(-22.0 * t))
                        )

                    sin(phase) * exp(-9.0 * t)
                }

                // Snare
                1 -> {
                    noise * exp(-18.0 * t) * 0.75 +
                    sin(2.0 * PI * 190.0 * t) *
                    exp(-20.0 * t) * 0.25
                }

                // Hi-hat
                2 -> {
                    noise * exp(-55.0 * t) * 0.55
                }

                // Clap
                else -> {
                    noise * exp(-20.0 * t) *
                    (0.6 + 0.4 * cos(2.0 * PI * 25.0 * t))
                }
            }

            output[start + i] +=
                (value * 0.65).toFloat()
        }
    }

    private fun addMetronome(
        output: FloatArray,
        start: Int,
        accent: Boolean
    ) {

        val length = min(
            (0.045 * sampleRate).toInt(),
            output.size - start
        )

        val frequency =
            if (accent) 1500.0 else 1000.0

        for (i in 0 until length) {

            val t = i.toDouble() / sampleRate

            val sound =
                sin(2.0 * PI * frequency * t) *
                exp(-90.0 * t)

            output[start + i] +=
                (sound * 0.20).toFloat()
        }
    }

    private fun saveProject() {

        try {
            val json = JSONObject()

            json.put("name", "CRE Project")
            json.put("bpm", bpm)
            json.put("metronome", metronome)

            val trackData = JSONArray()

            for (track in 0 until 4) {

                val steps = JSONArray()

                for (step in 0 until 16) {
                    steps.put(pattern[track][step])
                }

                trackData.put(steps)
            }

            json.put("patterns", trackData)

            File(
                filesDir,
                "cre_project.json"
            ).writeText(json.toString())

            toast("Project saved offline")

        } catch (error: Exception) {
            toast("Save failed: ${error.message}")
        }
    }

    private fun loadProject() {

        try {
            val file = File(
                filesDir,
                "cre_project.json"
            )

            if (!file.exists()) {
                toast("No saved project found")
                return
            }

            val json = JSONObject(file.readText())

            bpm = json.getInt("bpm")
            metronome = json.getBoolean("metronome")

            val data = json.getJSONArray("patterns")

            for (track in 0 until 4) {
                val steps = data.getJSONArray(track)

                for (step in 0 until 16) {
                    pattern[track][step] =
                        steps.getBoolean(step)
                }
            }

            bpmLabel.text = "BPM: $bpm"

            refreshGrid()

            toast("Project loaded")

            if (isPlaying) startPlayback()

        } catch (error: Exception) {
            toast("Load failed: ${error.message}")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        stopAudio()
        super.onDestroy()
    }
}


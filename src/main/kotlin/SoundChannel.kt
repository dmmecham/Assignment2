import kotlin.math.PI

class SoundChannel(
    private val waveform: Waveform,
    private val measures: List<String>
) : AudioChannel {
    override fun getDuration(song: Song): Double {
        var duration = 0.0
        val beatDuration = 60.0 / song.tempo
        
        for (measure in measures) {
            val tokens = measure.trim().split(Regex("\\s+"))
            var i = 0
            while (i < tokens.size) {
                val noteDuration = tokens.getOrNull(i + 1)?.toDoubleOrNull()
                if (noteDuration == null || noteDuration <= 0) {
                    throw IllegalArgumentException("Invalid note duration")
                }
                duration += noteDuration * beatDuration
                i += 2
            }
        }
        
        return duration
    }
    
    override fun generate(song: Song, duration: Double): DoubleArray {
        val totalSamples = (song.sampleRate * duration).toInt()
        val samples = DoubleArray(totalSamples)
        
        var sampleIndex = 0
        var phase = 0.0
        val beatDurationSamples = song.sampleRate * 60.0 / song.tempo
        
        for (measure in measures) {
            val tokens = measure.trim().split(Regex("\\s+"))
            
            var i = 0
            while (i < tokens.size) {
                val note = tokens[i]
                val duration = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 1.0
                i += 2
                
                val noteDurationSamples = (duration * beatDurationSamples).toInt()
                val frequency = NoteFrequency.frequency(note)
                
                for (j in 0 until noteDurationSamples) {
                    if (sampleIndex < totalSamples) {
                        samples[sampleIndex] = if (frequency != null) {
                            waveform.generate(frequency, song.sampleRate, phase)
                        } else {
                            0.0
                        }
                        
                        if (frequency != null) {
                            phase = (phase + 2 * PI * frequency / song.sampleRate) % (2 * PI)
                        }
                        sampleIndex++
                    }
                }
            }
        }
        
        return samples
    }
}
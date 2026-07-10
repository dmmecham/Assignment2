import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class SoundSynthesis(private val song: Song) {
    
    fun synthesize(): DoubleArray {
        val totalDuration = calculateDuration()
        val channels = mutableListOf<DoubleArray>()
        
        for (channel in song.channels) {
            channels.add(channel.generate(song, totalDuration))
        }
        
        return mixChannels(channels, song.sampleRate, totalDuration)
    }
    
    private fun calculateDuration(): Double {
        var maxDuration = 0.0
        
        for (channel in song.channels) {
            val duration = channel.getDuration(song)
            maxDuration = maxOf(maxDuration, duration)
        }
        
        return if (maxDuration > 0) maxDuration else 1.0
    }
    
    private fun mixChannels(channels: List<DoubleArray>, sampleRate: Int, duration: Double): DoubleArray {
        val totalSamples = (sampleRate * duration).toInt()
        val mixed = DoubleArray(totalSamples)
        
        for (channel in channels) {
            for (i in channel.indices) {
                if (i < totalSamples) {
                    mixed[i] += channel[i]
                }
            }
        }
        
        // Normalize to prevent clipping
        val maxAmplitude =
            mixed.maxOfOrNull { kotlin.math.abs(it) } ?: 1.0
        if (maxAmplitude > 1.0) {
            for (i in mixed.indices) {
                mixed[i] /= maxAmplitude
            }
        }
        
        return mixed
    }
    
    fun play() {
        val samples = synthesize()
        val bitDepth = 16
        val numChannels = 1
        
        val format = AudioFormat(song.sampleRate.toFloat(), bitDepth, numChannels, true, false)
        val line: SourceDataLine = AudioSystem.getSourceDataLine(format)
        
        val buffer = ByteArray(samples.size * 2)
        for (i in samples.indices) {
            val pcmValue = (samples[i] * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767)
            buffer[i * 2] = pcmValue.toByte()
            buffer[i * 2 + 1] = (pcmValue shr 8).toByte()
        }
        
        line.open(format)
        line.start()
        line.write(buffer, 0, buffer.size)
        line.drain()
        line.stop()
        line.close()
    }
}
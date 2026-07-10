import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class SynthesisIntegrationTest {
    
    private fun createTempFile(content: String): String {
        val file = createTempFile()
        file.writeText(content)
        return file.toString()
    }
    
    @Test
    fun testSingleChannelSynthesis() {
        val content = """
44100 4 120
sin | C4 1 D4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // Should generate approximately 1 second of audio (2 beats at 120 BPM)
            val expectedSamples = 44100 * (2.0 / 2.0) // 2 beats, each beat = 0.5 seconds
            assertTrue(samples.size > 20000, "Should generate enough samples")
            assertTrue(samples.all { it >= -1.0 && it <= 1.0 }, "All samples should be in [-1, 1] range")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testMultiChannelSynthesis() {
        val content = """
44100 4 120
sin | C4 2 D4 2
square | E4 2 F4 2
saw | - 2 - 2
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertEquals(3, song.channels.size)
            
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // Just verify that we generate valid output
            assertTrue(samples.isNotEmpty(), "Should generate some samples")
            
            // Check for out-of-range samples and report max
            val maxSample = samples.maxOrNull() ?: 0.0
            val minSample = samples.minOrNull() ?: 0.0
            assertTrue(maxSample <= 1.01, "Max sample too high: $maxSample")
            assertTrue(minSample >= -1.01, "Min sample too low: $minSample")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testEffectApplication() {
        val content = """
44100 4 120
sin vol${'$'}0.5 | C4 1 D4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // With volume at 0.5, max amplitude should be lower
            val maxAmplitude = samples.maxOrNull() ?: 0.0
            assertTrue(maxAmplitude <= 0.6, "Volume effect should reduce amplitude")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testMixing() {
        val content = """
44100 4 120
sin vol${'$'}0.3 | C4 2
square vol${'$'}0.3 | C4 2
saw vol${'$'}0.3 | C4 2
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // Combined channels should not exceed normalized amplitude
            val maxAmplitude = samples.maxOrNull() ?: 0.0
            assertTrue(maxAmplitude >= 0.1, "Mixed channels should produce non-silent output")
            assertTrue(maxAmplitude <= 1.0, "Output should be normalized")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testDurationCalculation() {
        val content = """
44100 4 120
sin | C4 1 D4 2 E4 0.5
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            
            // 1 + 2 + 0.5 = 3.5 beats = 1.75 seconds at 120 BPM
            val duration = synth.synthesize().size / 44100.0
            assertTrue(duration >= 1.7 && duration <= 1.8, "Duration should be correct: $duration")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testWhiteNoiseVariation() {
        val content = """
44100 4 120
whitenoise | C4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // White noise samples should have variety (not all the same)
            val uniqueValues = samples.toSet().size
            assertTrue(uniqueValues > 1000, "White noise should have many unique values")
        } finally {
            File(filename).delete()
        }
    }
}

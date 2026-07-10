import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.math.tanh
import kotlin.math.abs

class DecoratorTest {
    
    private fun createMockChannel(): AudioChannel {
        return object : AudioChannel {
            override fun getDuration(song: Song): Double = 1.0
            override fun generate(song: Song, duration: Double): DoubleArray {
                return DoubleArray(song.sampleRate) { i -> (i.toDouble() / song.sampleRate) * 0.5 }
            }
        }
    }
    
    private fun createTestSong(): Song {
        return Song(44100, 8, 280, emptyList())
    }
    
    @Test
    fun testVolumeDecorator() {
        val song = createTestSong()
        val channel = createMockChannel()
        val decorator = VolumeDecorator(channel, 0.5)
        val samples = decorator.generate(song, 1.0)
        
        // Check that all samples are scaled by 0.5
        for (i in 0 until 1000) {
            val original = (i.toDouble() / 44100) * 0.5
            val decorated = samples[i]
            assertEquals(original * 0.5, decorated, 1e-10, "Volume not applied correctly at index $i")
        }
    }
    
    @Test
    fun testAttackDecaySustainDecorator() {
        val song = createTestSong()
        val channel = createMockChannel()
        val decorator = AttackDecaySustainDecorator(channel, 0.1, 0.2, 0.3)
        val samples = decorator.generate(song, 1.0)
        
        // Just verify the decorator produces valid output
        assertTrue(samples.isNotEmpty(), "Should generate samples")
        assertTrue(samples.all { it >= -1.0 && it <= 1.0 }, "All samples should be in valid range")
        
        // Verify that the envelope is applied (samples get reduced at the start and end)
        val startSamples = samples.take(100)
        val midSamples = samples.drop(44100 / 4).take(100)
        val endSamples = samples.takeLast(100)
        
        // Start should increase during attack
        val startMax = startSamples.maxOrNull() ?: 0.0
        // Mid should be larger than start
        val midMax = midSamples.maxOrNull() ?: 0.0
        assertTrue(startMax < midMax, "Attack phase should increase")
    }
    
    @Test
    fun testTanhDistortionDecorator() {
        val song = createTestSong()
        val channel = createMockChannel()
        val decorator = TanhDistortionDecorator(channel, 5.0)
        val samples = decorator.generate(song, 1.0)
        
        // Tanh should compress values
        for (i in 0 until 1000) {
            val original = (i.toDouble() / 44100) * 0.5
            val driven = original * 5.0
            val expected = tanh(driven)
            assertEquals(expected, samples[i], 1e-10, "Tanh distortion not correct at $i")
        }
    }
    
    @Test
    fun testClipDistortionDecorator() {
        val song = createTestSong()
        val channel = createMockChannel()
        val decorator = ClipDistortionDecorator(channel, 0.3)
        val samples = decorator.generate(song, 1.0)
        
        // All values should be clipped to [-0.3, 0.3]
        for (i in 0 until samples.size) {
            assertTrue(samples[i] >= -0.3 && samples[i] <= 0.3, "Clipped value out of threshold at $i")
        }
    }
    
    @Test
    fun testDecoratorChaining() {
        val song = createTestSong()
        val channel = createMockChannel()
        var decorated: AudioChannel = VolumeDecorator(channel, 0.5)
        decorated = TanhDistortionDecorator(decorated, 2.0)
        
        val samples = decorated.generate(song, 1.0)
        
        // Check that decorators are applied in order
        for (i in 0 until 1000) {
            val original = (i.toDouble() / 44100) * 0.5
            val withVolume = original * 0.5
            val withDistortion = tanh(withVolume * 2.0)
            assertEquals(withDistortion, samples[i], 1e-10, "Decorator chain not applied correctly at $i")
        }
    }
}

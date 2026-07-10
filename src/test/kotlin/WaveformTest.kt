import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.abs

class WaveformTest {
    
    @Test
    fun testSineWave() {
        val waveform = SineWaveStrategy()
        val sampleRate = 44100
        val frequency = 440.0
        
        // Generate two samples and check they're different (since it's a sine wave)
        val sample1 = waveform.generate(frequency, sampleRate, 0.0)
        val sample2 = waveform.generate(frequency, sampleRate, PI / 4)
        
        // Both should be in [-1, 1] range
        assertTrue(sample1 >= -1.0 && sample1 <= 1.0, "Sine sample out of range: $sample1")
        assertTrue(sample2 >= -1.0 && sample2 <= 1.0, "Sine sample out of range: $sample2")
        
        // Should be different (very unlikely they're exactly equal)
        assertFalse(sample1 == sample2, "Sine samples should be different")
    }
    
    @Test
    fun testSquareWave() {
        val waveform = SquareWaveStrategy()
        val sampleRate = 44100
        val frequency = 440.0
        
        val sample1 = waveform.generate(frequency, sampleRate, 0.0)
        val sample2 = waveform.generate(frequency, sampleRate, PI)
        
        // Square wave should be exactly +1 or -1
        assertTrue(sample1 == 1.0 || sample1 == -1.0, "Square wave sample not ±1: $sample1")
        assertTrue(sample2 == 1.0 || sample2 == -1.0, "Square wave sample not ±1: $sample2")
    }
    
    @Test
    fun testSawWave() {
        val waveform = SawWaveStrategy()
        val sampleRate = 44100
        val frequency = 440.0
        
        val sample1 = waveform.generate(frequency, sampleRate, 0.0)
        val sample2 = waveform.generate(frequency, sampleRate, PI)
        
        // Sawtooth should be in [-1, 1] range
        assertTrue(sample1 >= -1.0 && sample1 <= 1.0, "Saw sample out of range: $sample1")
        assertTrue(sample2 >= -1.0 && sample2 <= 1.0, "Saw sample out of range: $sample2")
    }
    
    @Test
    fun testWhiteNoise() {
        val waveform = WhiteNoiseStrategy()
        val sampleRate = 44100
        val frequency = 440.0
        
        val sample1 = waveform.generate(frequency, sampleRate, 0.0)
        val sample2 = waveform.generate(frequency, sampleRate, PI)
        
        // Noise should be in [-1, 1] range
        assertTrue(sample1 >= -1.0 && sample1 <= 1.0, "Noise sample out of range: $sample1")
        assertTrue(sample2 >= -1.0 && sample2 <= 1.0, "Noise sample out of range: $sample2")
        
        // Each sample should be random (very unlikely they're equal)
        assertFalse(sample1 == sample2, "Noise samples should be different")
    }
    
    @Test
    fun testWaveformFactory() {
        assertTrue(WaveformFactory.create("sin") is SineWaveStrategy)
        assertTrue(WaveformFactory.create("square") is SquareWaveStrategy)
        assertTrue(WaveformFactory.create("saw") is SawWaveStrategy)
        assertTrue(WaveformFactory.create("whitenoise") is WhiteNoiseStrategy)
    }
}

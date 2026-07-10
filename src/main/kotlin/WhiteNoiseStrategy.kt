import kotlin.random.Random

class WhiteNoiseStrategy : Waveform {
    private val random = Random(System.currentTimeMillis())
    
    override fun generate(frequency: Double, sampleRate: Int, phase: Double): Double {
        return random.nextDouble() * 2.0 - 1.0
    }
}
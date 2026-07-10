import kotlin.math.PI

class SawWaveStrategy : Waveform {
    override fun generate(frequency: Double, sampleRate: Int, phase: Double): Double {
        val normalizedPhase = phase % (2 * PI)
        return 2.0 * (normalizedPhase / (2 * PI)) - 1.0
    }
}
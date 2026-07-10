import kotlin.math.PI

class SquareWaveStrategy : Waveform {
    override fun generate(frequency: Double, sampleRate: Int, phase: Double): Double {
        val normalizedPhase = phase % (2 * PI)
        return if (normalizedPhase < PI) 1.0 else -1.0
    }
}
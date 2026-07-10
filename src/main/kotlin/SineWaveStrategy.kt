import kotlin.math.sin

class SineWaveStrategy : Waveform {
    override fun generate(frequency: Double, sampleRate: Int, phase: Double): Double {
        return sin(phase)
    }
}
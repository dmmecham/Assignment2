interface Waveform {
    fun generate(frequency: Double, sampleRate: Int, phase: Double): Double
}
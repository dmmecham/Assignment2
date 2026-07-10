object WaveformFactory {
    fun create(type: String): Waveform =
        when(type) {
            "saw" -> SawWaveStrategy()
            "sin" -> SineWaveStrategy()
            "square" -> SquareWaveStrategy()
            "whitenoise" -> WhiteNoiseStrategy()
            else -> throw UnknownWaveformException(type)
        }
}


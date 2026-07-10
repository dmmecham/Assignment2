class AttackDecaySustainDecorator(
    private val channel: AudioChannel,
    private val attackEnd: Double,
    private val decayEnd: Double,
    private val sustain: Double
) : AudioChannel {
    init {
        require(attackEnd >= 0.0) {
            "ADS attack time must be non-negative."
        }

        require(decayEnd >= attackEnd) {
            "ADS decay time must be greater than or equal to attack time."
        }

        require(sustain in 0.0..1.0) {
            "ADS sustain level must be between 0.0 and 1.0."
        }
    }

    override fun getDuration(song: Song): Double {
        return channel.getDuration(song)
    }
    
    override fun generate(song: Song, duration: Double): DoubleArray {
        val samples = channel.generate(song, duration)
        
        for (i in samples.indices) {
            val timeInSeconds = i.toDouble() / song.sampleRate
            val envelope = when {
                attackEnd == decayEnd -> sustain
                attackEnd == 0.0 && timeInSeconds < decayEnd ->
                    1.0 - (timeInSeconds / decayEnd) * (1.0 - sustain)

                timeInSeconds < attackEnd ->
                    timeInSeconds / attackEnd

                timeInSeconds < decayEnd ->
                    1.0 - ((timeInSeconds - attackEnd) /
                            (decayEnd - attackEnd)) * (1.0 - sustain)

                else ->
                    sustain
            }
            samples[i] *= envelope
        }
        
        return samples
    }
}
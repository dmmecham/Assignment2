object EffectFactory {
    fun create(effectString: String, channel: AudioChannel): AudioChannel {
        val parts = effectString.split('$')
        val effectType = parts.getOrNull(0) ?: error("Invalid effect format")
        
        return when (effectType) {
            "vol" -> {
                val level = parts.getOrNull(1)?.toDoubleOrNull() ?: error("Volume requires level")
                VolumeDecorator(channel, level)
            }
            "ads" -> {
                val attackEnd = parts.getOrNull(1)?.toDoubleOrNull() ?: error("ADS requires attackEnd")
                val decayEnd = parts.getOrNull(2)?.toDoubleOrNull() ?: error("ADS requires decayEnd")
                val sustain = parts.getOrNull(3)?.toDoubleOrNull() ?: error("ADS requires sustain")
                require(attackEnd >= 0.0)
                require(decayEnd >= attackEnd)
                require(sustain in 0.0..1.0)
                AttackDecaySustainDecorator(channel, attackEnd, decayEnd, sustain)
            }
            "tanh" -> {
                val drive = parts.getOrNull(1)?.toDoubleOrNull() ?: error("Tanh requires drive")
                TanhDistortionDecorator(channel, drive)
            }
            "clip" -> {
                val threshold = parts.getOrNull(1)?.toDoubleOrNull() ?: error("Clip requires threshold")
                ClipDistortionDecorator(channel, threshold)
            }
            else -> error("Unknown effect: $effectType")
        }
    }
}
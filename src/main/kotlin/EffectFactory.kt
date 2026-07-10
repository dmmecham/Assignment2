object EffectFactory {
    fun create(effectString: String, channel: AudioChannel): AudioChannel {
        val parts = effectString.split('$')
        val effectType = parts.getOrNull(0) 
            ?: throw EffectSyntaxException(effectString, "empty effect type")
        
        return when (effectType) {
            "vol" -> {
                val level = parts.getOrNull(1)?.toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "volume requires level parameter")
                VolumeDecorator(channel, level)
            }
            "ads" -> {
                val attackEnd = parts.getOrNull(1)?.toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "attack-decay-sustain requires attackEnd parameter")
                val decayEnd = parts.getOrNull(2)?.toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "attack-decay-sustain requires decayEnd parameter")
                val sustain = parts.getOrNull(3)?.toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "attack-decay-sustain requires sustain parameter")
                
                try {
                    require(attackEnd >= 0.0) { "attackEnd must be >= 0" }
                    require(decayEnd >= attackEnd) { "decayEnd must be >= attackEnd" }
                    require(sustain in 0.0..1.0) { "sustain must be between 0.0 and 1.0" }
                } catch (e: IllegalArgumentException) {
                    throw EffectSyntaxException(effectString, e.message ?: "invalid parameters")
                }
                
                AttackDecaySustainDecorator(channel, attackEnd, decayEnd, sustain)
            }
            "tanh" -> {
                val drive = parts.getOrNull(1)?.toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "tanh distortion requires drive parameter")
                TanhDistortionDecorator(channel, drive)
            }
            "clip" -> {
                val threshold = parts.getOrNull(1)?.toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "clip distortion requires threshold parameter")
                ClipDistortionDecorator(channel, threshold)
            }
            else -> throw EffectSyntaxException(effectString, "unknown effect type '$effectType'")
        }
    }
}
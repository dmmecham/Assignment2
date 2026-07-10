object EffectFactory {
    fun create(effectString: String, channel: AudioChannel): AudioChannel {
        val parts = effectString.split('$')
        val effectType = parts.getOrNull(0) 
            ?: throw EffectSyntaxException(effectString, "empty effect type")
        
        return when (effectType) {
            "vol" -> {
                // Validate parameter count: vol$level (exactly 2 parts total)
                if (parts.size != 2) {
                    throw EffectSyntaxException(
                        effectString, 
                        "volume takes exactly 1 parameter (level), but got ${parts.size - 1}"
                    )
                }
                
                val level = parts[1].toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "volume level must be a valid number")
                
                // Validate level is sensible (not NaN, not infinite)
                if (level.isNaN()) {
                    throw EffectSyntaxException(effectString, "volume level cannot be NaN")
                }
                if (level.isInfinite()) {
                    throw EffectSyntaxException(effectString, "volume level cannot be infinite")
                }
                if (level < 0.0) {
                    throw EffectSyntaxException(effectString, "volume level must be non-negative")
                }
                
                VolumeDecorator(channel, level)
            }
            "ads" -> {
                // Validate parameter count: ads$attackEnd$decayEnd$sustain (exactly 4 parts total)
                if (parts.size != 4) {
                    throw EffectSyntaxException(
                        effectString,
                        "attack-decay-sustain takes exactly 3 parameters (attackEnd, decayEnd, sustain), but got ${parts.size - 1}"
                    )
                }
                
                val attackEnd = parts[1].toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "attackEnd must be a valid number")
                val decayEnd = parts[2].toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "decayEnd must be a valid number")
                val sustain = parts[3].toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "sustain must be a valid number")
                
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
                // Validate parameter count: tanh$drive (exactly 2 parts total)
                if (parts.size != 2) {
                    throw EffectSyntaxException(
                        effectString,
                        "tanh distortion takes exactly 1 parameter (drive), but got ${parts.size - 1}"
                    )
                }
                
                val drive = parts[1].toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "tanh drive must be a valid number")
                
                // Validate drive is sensible (not NaN, not infinite, positive)
                if (drive.isNaN()) {
                    throw EffectSyntaxException(effectString, "tanh drive cannot be NaN")
                }
                if (drive.isInfinite()) {
                    throw EffectSyntaxException(effectString, "tanh drive cannot be infinite")
                }
                if (drive <= 0.0) {
                    throw EffectSyntaxException(effectString, "tanh drive must be positive")
                }
                
                TanhDistortionDecorator(channel, drive)
            }
            "clip" -> {
                // Validate parameter count: clip$threshold (exactly 2 parts total)
                if (parts.size != 2) {
                    throw EffectSyntaxException(
                        effectString,
                        "clip distortion takes exactly 1 parameter (threshold), but got ${parts.size - 1}"
                    )
                }
                
                val threshold = parts[1].toDoubleOrNull() 
                    ?: throw EffectSyntaxException(effectString, "clip threshold must be a valid number")
                
                // Validate threshold is sensible (positive, not NaN, not infinite)
                if (threshold.isNaN()) {
                    throw EffectSyntaxException(effectString, "clip threshold cannot be NaN")
                }
                if (threshold.isInfinite()) {
                    throw EffectSyntaxException(effectString, "clip threshold cannot be infinite")
                }
                if (threshold <= 0.0) {
                    throw EffectSyntaxException(effectString, "clip threshold must be positive")
                }
                
                ClipDistortionDecorator(channel, threshold)
            }
            else -> throw EffectSyntaxException(effectString, "unknown effect type '$effectType'")
        }
    }
}
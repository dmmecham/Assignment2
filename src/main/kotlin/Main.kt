import java.io.File

fun readSong(filename: String): Song {
    val file = File(filename)
    
    if (!file.exists()) {
        throw IllegalArgumentException("File not found: $filename")
    }
    
    val lines = try {
        file.readLines().filter { it.isNotBlank() }
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to read file: ${e.message}")
    }
    
    if (lines.isEmpty()) {
        throw IllegalArgumentException("File is empty.")
    }
    
    // Parse header
    val header = lines.first().trim().split(Regex("\\s+"))
    if (header.size != 3) {
        throw IllegalArgumentException("Header must contain: sampleRate beatsPerMeasure tempo")
    }
    
    val sampleRate = try {
        header[0].toInt()
    } catch (_: NumberFormatException) {
        throw IllegalArgumentException("Invalid sampleRate: ${header[0]}")
    }
    
    val beatsPerMeasure = try {
        header[1].toInt()
    } catch (_: NumberFormatException) {
        throw IllegalArgumentException("Invalid beatsPerMeasure: ${header[1]}")
    }
    
    val tempo = try {
        header[2].toInt()
    } catch (_: NumberFormatException) {
        throw IllegalArgumentException("Invalid tempo: ${header[2]}")
    }
    
    // Parse channels
    val channels = mutableListOf<AudioChannel>()
    for ((lineNum, line) in lines.drop(1).withIndex()) {
        try {
            val parts = line.split('|')
            if (parts.isEmpty()) {
                throw IllegalArgumentException("Invalid channel line: $line")
            }
            
            val settings = parts[0].trim().split(Regex("\\s+"))
            val measures = parts.drop(1).map(String::trim).filter { it.isNotEmpty() }
            
            if (settings.isEmpty()) {
                throw IllegalArgumentException("Channel must specify a waveform")
            }
            
            val waveform = WaveformFactory.create(settings[0])
            var channel: AudioChannel = SoundChannel(waveform, measures)
            
            // Apply effects in order
            for (effectSpec in settings.drop(1)) {
                channel = EffectFactory.create(effectSpec, channel)
            }
            
            channels.add(channel)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error parsing channel at line ${lineNum + 2}: ${e.message}")
        }
    }
    
    return Song(sampleRate, beatsPerMeasure, tempo, channels)
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -jar synthesizer.jar <song-file>")
        return
    }
    
    val filename = args[0]
    
    try {
        val song = readSong(filename)
        println("Successfully loaded song: $filename")
        println("Sample rate: ${song.sampleRate}")
        println("Beats per measure: ${song.beatsPerMeasure}")
        println("Tempo: ${song.tempo}")
        println("Channels: ${song.channels.size}")
        println("\nPlaying audio...")
        
        val synthesizer = SoundSynthesis(song)
        synthesizer.play()
        
        println("Playback complete!")
    } catch (e: IllegalArgumentException) {
        System.err.println("Error: ${e.message}")
        System.exit(1)
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}
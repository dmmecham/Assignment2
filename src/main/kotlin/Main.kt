import java.io.File
import kotlin.system.exitProcess

fun validateMeasure(measure: String, measureIdx: Int) {
    val tokens = measure.trim().split(Regex("\\s+"))
    
    if (tokens.isEmpty()) {
        throw MeasureSyntaxException(measureIdx, "measure is empty")
    }
    
    if (tokens.size % 2 != 0) {
        throw MeasureSyntaxException(
            measureIdx, 
            "has ${tokens.size} tokens - must have even number (note/duration pairs)"
        )
    }
    
    var i = 0
    while (i < tokens.size) {
        val note = tokens[i]
        val durationStr = tokens.getOrNull(i + 1) 
            ?: throw MeasureSyntaxException(measureIdx, "missing duration after note '$note'")
        
        // Validate note format
        validateNote(note)
        
        // Validate duration format
        try {
            val duration = durationStr.toDouble()
            if (duration <= 0) {
                throw InvalidDurationException(durationStr, "duration must be positive, got $duration")
            }
        } catch (e: NumberFormatException) {
            throw InvalidDurationException(durationStr, "must be a positive number")
        }
        
        i += 2
    }
}

fun validateNote(note: String) {
    // Allow rests (-)
    if (note == "-") return
    
    // Validate note format: [A-G][#b]?\d+
    val validNoteRegex = Regex("""^[A-G][#b]?-?\d+$""")
    if (!validNoteRegex.matches(note)) {
        throw InvalidNoteException(
            note,
            "must be in format like 'C4', 'A#4', 'Bb3', or '-' for rest"
        )
    }
    
    // Try to get frequency to validate it's a known note
    try {
        NoteFrequency.frequency(note)
    } catch (e: Exception) {
        throw InvalidNoteFrequencyException(note)
    }
}

fun readSong(filename: String): Song {
    val file = File(filename)
    
    if (!file.exists()) {
        throw FileSyntaxException(filename, "file not found")
    }
    
    val lines = try {
        file.readLines().filter { it.isNotBlank() }
    } catch (e: Exception) {
        throw FileSyntaxException(filename, "cannot read file", e)
    }
    
    if (lines.isEmpty()) {
        throw FileSyntaxException(filename, "file is empty")
    }
    
    // Parse header
    val header = lines.first().trim().split(Regex("\\s+"))
    if (header.size != 3) {
        throw HeaderSyntaxException("expected 3 values (sampleRate beatsPerMeasure tempo), got ${header.size}")
    }
    
    val sampleRate = try {
        header[0].toInt()
    } catch (_: NumberFormatException) {
        throw HeaderSyntaxException("invalid sampleRate '${header[0]}' - must be an integer")
    }
    
    val beatsPerMeasure = try {
        header[1].toInt()
    } catch (_: NumberFormatException) {
        throw HeaderSyntaxException("invalid beatsPerMeasure '${header[1]}' - must be an integer")
    }
    
    val tempo = try {
        header[2].toInt()
    } catch (_: NumberFormatException) {
        throw HeaderSyntaxException("invalid tempo '${header[2]}' - must be an integer")
    }
    
    // Parse channels
    val channels = mutableListOf<AudioChannel>()
    for ((lineNum, line) in lines.drop(1).withIndex()) {
        try {
            val parts = line.split('|')
            if (parts.isEmpty()) {
                throw ChannelSyntaxException(lineNum + 2, "empty channel line")
            }
            
            val settings = parts[0].trim().split(Regex("\\s+"))
            
            // Validate channel line structure strictly
            // Parts after settings are measures - check for empty measures
            val measureParts = parts.drop(1)
            
            // Remove trailing empty parts (from final |) which are allowed
            val measurePartsWithoutTrailing = if (measureParts.isNotEmpty() && measureParts.last().trim().isEmpty()) {
                measureParts.dropLast(1)
            } else {
                measureParts
            }
            
            // Check for interior empty measures (caused by repeated pipes)
            for ((measureIdx, measurePart) in measurePartsWithoutTrailing.withIndex()) {
                val trimmed = measurePart.trim()
                if (trimmed.isEmpty()) {
                    throw ChannelSyntaxException(
                        lineNum + 2,
                        "malformed channel line: empty measure at position $measureIdx (caused by repeated || or invalid pipe placement)"
                    )
                }
            }
            
            val measures = measurePartsWithoutTrailing.map(String::trim)
            
            if (settings.isEmpty()) {
                throw ChannelSyntaxException(lineNum + 2, "missing waveform (first token in settings)")
            }
            
            if (measures.isEmpty()) {
                throw ChannelSyntaxException(lineNum + 2, "channel must have at least one measure")
            }
            
            // Validate all measures have proper structure
            for ((measureIdx, measure) in measures.withIndex()) {
                validateMeasure(measure, measureIdx)
            }
            
            val waveform = WaveformFactory.create(settings[0])
            var channel: AudioChannel = SoundChannel(waveform, measures)
            
            // Apply effects in order
            for (effectSpec in settings.drop(1)) {
                channel = EffectFactory.create(effectSpec, channel)
            }
            
            channels.add(channel)
        } catch (e: SongParsingException) {
            // Re-throw parsing exceptions as-is
            throw e
        } catch (e: Exception) {
            // Wrap unexpected exceptions
            throw ChannelSyntaxException(lineNum + 2, e.message ?: "unknown error", e)
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
    } catch (e: SongParsingException) {
        System.err.println("Error: ${e.message}")
        exitProcess(1)
    } catch (e: SynthesizerException) {
        System.err.println("Synthesizer error: ${e.message}")
        exitProcess(1)
    } catch (e: Exception) {
        System.err.println("Unexpected error: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}
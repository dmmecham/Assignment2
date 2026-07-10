/**
 * Base exception for all synthesizer errors.
 * Provides a common parent type for error handling and distinguishes
 * synthesizer-specific errors from general exceptions.
 */
open class SynthesizerException(message: String, cause: Throwable? = null) : 
    Exception(message, cause)

/**
 * Exceptions related to song file parsing.
 * Thrown when the song file format is invalid or missing required data.
 */
open class SongParsingException(message: String, cause: Throwable? = null) : 
    SynthesizerException(message, cause)

/**
 * File I/O error during song parsing.
 * Thrown when a song file cannot be read or doesn't exist.
 */
class FileSyntaxException(filename: String, reason: String, cause: Throwable? = null) :
    SongParsingException("Cannot read song file '$filename': $reason", cause)

/**
 * Header line parsing error.
 * Thrown when the song header (first line) is malformed.
 * Expected format: sampleRate beatsPerMeasure tempo
 */
class HeaderSyntaxException(message: String, cause: Throwable? = null) :
    SongParsingException("Invalid song header: $message", cause)

/**
 * Channel line parsing error.
 * Thrown when a channel definition line is malformed or incomplete.
 */
class ChannelSyntaxException(lineNumber: Int, message: String, cause: Throwable? = null) :
    SongParsingException("Error in channel at line $lineNumber: $message", cause)

/**
 * Measure content parsing error.
 * Thrown when a measure contains invalid note/duration pairs.
 */
class MeasureSyntaxException(measureIndex: Int, message: String, cause: Throwable? = null) :
    SongParsingException("Invalid measure $measureIndex: $message", cause)

/**
 * Effect parameter parsing error.
 * Thrown when an effect specification is malformed or has invalid parameters.
 */
class EffectSyntaxException(effectSpec: String, message: String, cause: Throwable? = null) :
    SongParsingException("Invalid effect '$effectSpec': $message", cause)

/**
 * Invalid note specification.
 * Thrown when a note name doesn't match the expected format.
 */
class InvalidNoteException(note: String, reason: String) :
    SongParsingException("Invalid note '$note': $reason")

/**
 * Unknown note frequency.
 * Thrown when a note name cannot be mapped to a frequency.
 */
class InvalidNoteFrequencyException(note: String) :
    SongParsingException("Unknown note: '$note' - not a valid pitch in scientific notation")

/**
 * Invalid note duration.
 * Thrown when a note duration value is invalid (non-numeric, zero, or negative).
 */
class InvalidDurationException(duration: String, reason: String) :
    SongParsingException("Invalid duration '$duration': $reason")

/**
 * Unknown waveform type.
 * Thrown when a waveform type cannot be recognized.
 */
class UnknownWaveformException(waveformType: String) :
    SongParsingException("Unknown waveform type: '$waveformType' (supported: sin, square, saw, whitenoise)")


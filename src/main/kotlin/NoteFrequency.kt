import kotlin.math.pow

object NoteFrequency {
    private val noteOffsets = mapOf(
        "C" to -9,
        "C#" to -8, "Db" to -8,
        "D" to -7,
        "D#" to -6, "Eb" to -6,
        "E" to -5,
        "F" to -4,
        "F#" to -3, "Gb" to -3,
        "G" to -2,
        "G#" to -1, "Ab" to -1,
        "A" to 0,
        "A#" to 1, "Bb" to 1,
        "B" to 2,
    )

    fun frequency(note: String): Double? {
        // Handles a rest.
        if (note == "-") return null
        val match = Regex("""^([A-G][#b]?)(-?\d+)$""")
            .matchEntire(note)
            ?: throw InvalidNoteFrequencyException(note)

        val (pitch, octaveString) = match.destructured
        val octave = octaveString.toInt()

        // Notes are base pitch offsets that are calculated against the octave.
        val semitones =
            noteOffsets[pitch]!! + (octave - 4) * 12

        return 440.0 * 2.0.pow(semitones / 12.0)
    }
}
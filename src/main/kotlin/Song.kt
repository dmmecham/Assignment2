data class Song(
    val sampleRate: Int,
    val beatsPerMeasure: Int,
    val tempo: Int,
    val channels: List<AudioChannel>
)
interface AudioChannel {
    fun generate(song: Song, duration: Double): DoubleArray
    fun getDuration(song: Song): Double
    
    // Returns list of (startSamples, endSamples, durationSamples) for each note
    fun getNoteBoundaries(song: Song): List<Triple<Int, Int, Int>> {
        return emptyList()
    }
}
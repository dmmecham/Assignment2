interface AudioChannel {
    fun generate(song: Song, duration: Double): DoubleArray
    fun getDuration(song: Song): Double
}
class VolumeDecorator(private val channel: AudioChannel, private val level: Double) : AudioChannel {
    override fun getDuration(song: Song): Double {
        return channel.getDuration(song)
    }
    
    override fun getNoteBoundaries(song: Song): List<Triple<Int, Int, Int>> {
        return channel.getNoteBoundaries(song)
    }
    
    override fun generate(song: Song, duration: Double): DoubleArray {
        val samples = channel.generate(song, duration)
        return DoubleArray(samples.size) { i -> samples[i] * level }
    }
}
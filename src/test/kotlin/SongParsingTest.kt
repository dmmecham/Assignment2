import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

class SongParsingTest {
    
    @Test
    fun testAllSongsCanBeParsed() {
        val songsDir = "/home/dmecham/Downloads/songs"
        val songFiles = File(songsDir).listFiles { f -> f.name.endsWith(".txt") }?.toList() ?: emptyList()
        
        assertTrue(songFiles.isNotEmpty(), "Should find song files")
        
        var parsedCount = 0
        for (file in songFiles) {
            try {
                val song = readSong(file.absolutePath)
                assertTrue(song.channels.isNotEmpty(), "${file.name} should have at least one channel")
                assertTrue(song.sampleRate > 0, "${file.name} should have valid sample rate")
                assertTrue(song.tempo > 0, "${file.name} should have valid tempo")
                parsedCount++
            } catch (e: Exception) {
                throw AssertionError("Failed to parse ${file.name}: ${e.message}", e)
            }
        }
        
        assertEquals(songFiles.size, parsedCount, "All songs should be parseable")
    }
    
    @Test
    fun testWaveSineCanBeSynthesized() {
        val song = readSong("/home/dmecham/Downloads/songs/wave_sine.txt")
        val synth = SoundSynthesis(song)
        val samples = synth.synthesize()
        
        assertTrue(samples.isNotEmpty(), "Should generate samples")
        assertTrue(samples.all { it >= -1.01 && it <= 1.01 }, "All samples should be in valid range")
    }
    
    @Test
    fun testComplexSongCanBeSynthesized() {
        val song = readSong("/home/dmecham/Downloads/songs/sarias_song.txt")
        val synth = SoundSynthesis(song)
        val samples = synth.synthesize()
        
        assertTrue(samples.isNotEmpty(), "Should generate samples for complex song")
        assertTrue(samples.all { it >= -1.01 && it <= 1.01 }, "All samples should be in valid range")
    }
}

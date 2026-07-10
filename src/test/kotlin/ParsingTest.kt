import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class ParsingTest {
    
    private fun createTempFile(content: String): String {
        val file = createTempFile()
        file.writeText(content)
        return file.toString()
    }
    
    @Test
    fun testValidHeader() {
        val content = """
44100 4 120
sin | C4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertEquals(44100, song.sampleRate)
            assertEquals(4, song.beatsPerMeasure)
            assertEquals(120, song.tempo)
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testMissingFile() {
        assertFailsWith<IllegalArgumentException> {
            readSong("/nonexistent/file/path.txt")
        }
    }
    
    @Test
    fun testEmptyFile() {
        val filename = createTempFile("")
        try {
            assertFailsWith<IllegalArgumentException> {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testInvalidHeader() {
        val content = "44100 4"  // Missing tempo
        val filename = createTempFile(content)
        try {
            assertFailsWith<IllegalArgumentException> {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testInvalidSampleRate() {
        val content = """
abc 4 120
sin | C4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<IllegalArgumentException> {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testSingleChannel() {
        val content = """
44100 4 120
sin | C4 1 D4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertEquals(1, song.channels.size)
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testMultipleChannels() {
        val content = """
44100 4 120
sin | C4 1 D4 1
square | E4 1 F4 1
saw | G4 1 A4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertEquals(3, song.channels.size)
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testEffects() {
        val content = """
44100 4 120
sin vol${'$'}0.8 ads${'$'}0.01${'$'}0.2${'$'}0.1 | C4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertEquals(1, song.channels.size)
            // Decorators wrap the base channel
            assertTrue(song.channels[0] is AttackDecaySustainDecorator || 
                      song.channels[0] is VolumeDecorator)
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testNoteFrequency() {
        assertEquals(440.0, NoteFrequency.frequency("A4") ?: 0.0, 0.01)
        assertEquals(261.63, NoteFrequency.frequency("C4") ?: 0.0, 0.01)
        assertEquals(null, NoteFrequency.frequency("-"))
    }
    
    @Test
    fun testRests() {
        val content = """
44100 4 120
sin | C4 1 - 1 D4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertEquals(1, song.channels.size)
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testFractionalDuration() {
        val content = """
44100 4 120
sin | C4 0.5 D4 1.5
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertEquals(1, song.channels.size)
        } finally {
            File(filename).delete()
        }
    }
}

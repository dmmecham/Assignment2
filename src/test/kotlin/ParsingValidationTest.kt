import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class ParsingValidationTest {
    
    private fun createTempFile(content: String): String {
        val file = createTempFile()
        file.writeText(content)
        return file.toString()
    }
    
    @Test
    fun testRejectMissingMeasures() {
        val content = """
44100 4 120
sin|
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<ChannelSyntaxException>("Should reject channels with no measures") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectOddTokensInMeasure() {
        val content = """
44100 4 120
sin|C4 1 D4
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<MeasureSyntaxException>("Should reject measure with odd number of tokens") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectInvalidNoteName() {
        val content = """
44100 4 120
sin|H4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<InvalidNoteException>("Should reject invalid note name H4") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectInvalidNoteSyntax() {
        val content = """
44100 4 120
sin|C 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<InvalidNoteException>("Should reject note without octave") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectNegativeDuration() {
        val content = """
44100 4 120
sin|C4 -1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<InvalidDurationException>("Should reject negative duration") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectZeroDuration() {
        val content = """
44100 4 120
sin|C4 0
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<InvalidDurationException>("Should reject zero duration") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectInvalidDurationFormat() {
        val content = """
44100 4 120
sin|C4 abc
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<InvalidDurationException>("Should reject non-numeric duration") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testAcceptValidSharps() {
        val content = """
44100 4 120
sin|C#4 1 D#4 1 F#4 1 G#4 1 A#4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertTrue(song.channels.isNotEmpty())
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testAcceptValidFlats() {
        val content = """
44100 4 120
sin|Db4 1 Eb4 1 Gb4 1 Ab4 1 Bb4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertTrue(song.channels.isNotEmpty())
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testAcceptFractionalDurations() {
        val content = """
44100 4 120
sin|C4 0.5 D4 1.5 E4 2.25
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertTrue(song.channels.isNotEmpty())
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testAcceptValidRests() {
        val content = """
44100 4 120
sin|- 1 C4 1 - 2 D4 0.5
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            assertTrue(song.channels.isNotEmpty())
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testAcceptTrailingPipe() {
        val content = """
44100 4 120
sin|C4 1|
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            // Trailing pipe is part of standard format - should be accepted
            val song = readSong(filename)
            assertTrue(song.channels.isNotEmpty())
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectInteriorEmptyMeasures() {
        val content = """
44100 4 120
sin|C4 1||D4 1|
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            // Double pipes create empty measure in middle - should be rejected
            assertFailsWith<ChannelSyntaxException>("Should reject empty measure from double pipe") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectLeadingDoubleStringPipes() {
        val content = """
44100 4 120
sin||C4 1|
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            // Leading double pipe creates empty measure - should be rejected
            assertFailsWith<ChannelSyntaxException>("Should reject leading double pipe") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectWhitespaceOnlyInteriorMeasure() {
        val content = """
44100 4 120
sin|C4 1|  |D4 1|
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            // Whitespace-only interior measure - should be rejected
            assertFailsWith<ChannelSyntaxException>("Should reject whitespace-only measure") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectInvalidWaveform() {
        val content = """
44100 4 120
triangle|C4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<UnknownWaveformException>("Should reject invalid waveform") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRejectInvalidEffect() {
        val content = """
44100 4 120
sin reverb$0.5|C4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            assertFailsWith<EffectSyntaxException>("Should reject invalid effect") {
                readSong(filename)
            }
        } finally {
            File(filename).delete()
        }
    }
}

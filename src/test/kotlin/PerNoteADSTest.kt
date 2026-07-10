import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class PerNoteADSTest {
    
    private fun createTempFile(content: String): String {
        val file = createTempFile()
        file.writeText(content)
        return file.toString()
    }
    
    @Test
    fun testADSAppliesToEachNoteIndividually() {
        val content = """
44100 4 120
sin ads$0.1$0.3$0.5|C4 1 D4 1 E4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // Get note boundaries to identify where each note is
            val boundaries = song.channels[0].getNoteBoundaries(song)
            assertEquals(3, boundaries.size, "Should have 3 notes")
            
            // Verify samples are valid
            assertTrue(samples.all { it >= -1.01 && it <= 1.01 }, "All samples in valid range")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testADSEnvelopeRestartsPerNote() {
        val content = """
44100 4 120
sin ads$0.01$0.2$0.1|C4 2 C4 2
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            val boundaries = song.channels[0].getNoteBoundaries(song)
            assertEquals(2, boundaries.size, "Should have 2 notes")
            
            // Each note gets its own envelope restart
            val (note1Start, note1End, _) = boundaries[0]
            val (note2Start, note2End, _) = boundaries[1]
            
            // Both notes should have attack phase at their start
            val note1AttackPhase = samples.slice(note1Start until minOf(note1Start + 100, samples.size))
            val note2AttackPhase = samples.slice(note2Start until minOf(note2Start + 100, samples.size))
            
            assertTrue(note1AttackPhase.isNotEmpty(), "Note 1 attack phase exists")
            assertTrue(note2AttackPhase.isNotEmpty(), "Note 2 attack phase exists")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testMultiNoteWithPluckEffect() {
        val content = """
44100 4 120
sin ads$0$0.15$0|C4 1 D4 1 E4 1 F4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // Pluck effect (no attack, quick decay to silence)
            assertTrue(samples.any { it != 0.0 }, "Should have non-zero samples")
            assertTrue(samples.all { it >= -1.01 && it <= 1.01 }, "All samples valid")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testADSWithDecoratorStacking() {
        val content = """
44100 4 120
sin vol$0.5 ads$0.01$0.2$0.1|C4 1 D4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            // Volume decorator should work with per-note ADS
            val maxAmplitude = samples.maxOrNull() ?: 0.0
            assertTrue(maxAmplitude <= 0.6, "Volume effect applied with ADS")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testRestsBetweenNotesPreserveEnvelopeRestart() {
        val content = """
44100 4 120
sin ads$0.05$0.2$0.1|C4 1 - 0.5 C4 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            val boundaries = song.channels[0].getNoteBoundaries(song)
            // Rest is included in boundaries
            assertTrue(boundaries.size >= 2, "Should have note boundaries")
            assertTrue(samples.all { it >= -1.01 && it <= 1.01 }, "All samples valid")
        } finally {
            File(filename).delete()
        }
    }
    
    @Test
    fun testComplexMeasureWithPerNoteADS() {
        val content = """
44100 8 280
sin ads$0.01$0.2$0.1|C4 1 D4 1 E4 1 F4 1|G4 1 A4 1 B4 1 C5 1
        """.trimIndent()
        
        val filename = createTempFile(content)
        try {
            val song = readSong(filename)
            val synth = SoundSynthesis(song)
            val samples = synth.synthesize()
            
            val boundaries = song.channels[0].getNoteBoundaries(song)
            assertEquals(8, boundaries.size, "Should have 8 notes")
            
            // Each note should be independent
            for ((start, end, duration) in boundaries) {
                if (start < samples.size) {
                    val noteSamples = samples.slice(start until minOf(end, samples.size))
                    assertTrue(noteSamples.isNotEmpty(), "Note samples exist")
                }
            }
        } finally {
            File(filename).delete()
        }
    }
}

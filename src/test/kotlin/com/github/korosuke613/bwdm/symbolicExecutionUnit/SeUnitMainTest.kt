package com.github.korosuke613.bwdm.symbolicExecutionUnit

import com.github.korosuke613.bwdm.informationStore.InformationExtractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SeUnitMainTest {
    private lateinit var informationExtractor: InformationExtractor
    private lateinit var seUnitMain: SeUnitMain
    private lateinit var fileName: String

    @Nested
    inner class Mod {
        @BeforeEach
        fun setup() {
            val resource = InformationExtractor::class.java.classLoader.getResource("problem.vdmpp")
            fileName = if (resource?.path == null) "" else resource.path
            informationExtractor = InformationExtractor(fileName)
            seUnitMain = SeUnitMain(informationExtractor.explicitFunctions["problemFunction"]!!)
        }

        @Test
        fun getAllTestcasesBySeTest() {
            val except = """
                No.1 : 96 -> "a mod 4 = 0 and a > 92"
                No.2 : 4 -> "a mod 4 = 0 and !a > 92"
                No.3 : 1 -> "!a mod 4 = 0"
                
            """.trimIndent()
            val actual = seUnitMain.allTestCases
            assertEquals(except, actual)
        }
    }
}
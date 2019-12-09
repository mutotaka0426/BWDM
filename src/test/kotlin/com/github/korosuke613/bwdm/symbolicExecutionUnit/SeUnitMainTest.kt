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
        fun getFunctionName() {
            assertEquals("problemFunction", informationExtractor.explicitFunctions["problemFunction"]?.functionName)
        }
    }
}
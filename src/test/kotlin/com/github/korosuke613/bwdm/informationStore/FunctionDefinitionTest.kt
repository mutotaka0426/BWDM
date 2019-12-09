package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.tc.definitions.TCExplicitOperationDefinition
import com.fujitsu.vdmj.tc.definitions.TCInstanceVariableDefinition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class FunctionDefinitionTest {
    private lateinit var informationExtractor: InformationExtractor
    private lateinit var fileName: String


    @BeforeEach
    fun setup() {
        val resource = InformationExtractor::class.java.classLoader.getResource("function.vdmpp")
        fileName = if (resource?.path == null) "" else resource.path
        informationExtractor = InformationExtractor(fileName)
    }

    @Test
    fun getFunctionName() {
        assertEquals("うるう年判定仕様", informationExtractor.explicitFunctions["うるう年判定仕様"]?.functionName)
    }

    @Test
    fun getVdmFilePath() {
        assertEquals(fileName, informationExtractor.vdmFilePath)
    }
}
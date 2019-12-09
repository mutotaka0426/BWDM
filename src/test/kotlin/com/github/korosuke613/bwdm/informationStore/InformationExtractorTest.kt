package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.tc.definitions.TCInstanceVariableDefinition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class InformationExtractorTest {
    private lateinit var informationExtractor: InformationExtractor
    private lateinit var fileName: String

    @Nested
    inner class Function {
        @BeforeEach
        fun setup() {
            val resource = InformationExtractor::class.java.classLoader.getResource("function.vdmpp")
            fileName = if (resource?.path == null) "" else resource.path
            informationExtractor = InformationExtractor(fileName)
        }

        @Test
        fun getFunctionName() {
            assertEquals("うるう年判定仕様", informationExtractor.functionName)
        }

        @Test
        fun getVdmFilePath() {
            assertEquals(fileName, informationExtractor.vdmFilePath)
        }
    }

    @Nested
    inner class Operation {
        @BeforeEach
        fun setup(){
            val resource = InformationExtractor::class.java.classLoader.getResource("operation.vdmpp")
            fileName = if (resource?.path == null) "" else resource.path
            informationExtractor = InformationExtractor(fileName)
        }

        @Test
        fun getOperationName1() {
            assertEquals("うるう年判定仕様", informationExtractor.explicitOperations["うるう年判定仕様"]?.name.toString())
        }


        @Test
        fun getInstanceVariables() {
            val expected: Array<String> = arrayOf("rule1", "rule2", "rule3")

            var i = 0
            informationExtractor.instanceVariables.values.forEach { instanceVariable: TCInstanceVariableDefinition ->
                assertEquals(expected[i], instanceVariable.name.toString())
                i++
            }
        }

        @Test
        fun getVdmFilePath() {
            assertEquals(fileName, informationExtractor.vdmFilePath)
        }
    }
}
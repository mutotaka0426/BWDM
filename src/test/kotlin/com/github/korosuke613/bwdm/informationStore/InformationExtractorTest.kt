package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.tc.definitions.TCExplicitOperationDefinition
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
        fun getOperationNames() {
            val expected: Array<String> = arrayOf("西暦設定", "うるう年判定")

            var i = 0
            informationExtractor.explicitOperations.values.forEach { instanceVariable: TCExplicitOperationDefinition ->
                assertEquals(expected[i], informationExtractor.explicitOperations[expected[i]]?.name.toString())
                i++
            }
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
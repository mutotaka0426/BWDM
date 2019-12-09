package com.github.korosuke613.bwdm.informationStore

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InformationExtractorTest {
    lateinit var informationExtractor: InformationExtractor
    private lateinit var fileName: String

    @BeforeEach
    fun setup(){
        val resource = InformationExtractor::class.java.getClassLoader().getResource("function.vdmpp")
        fileName = if (resource?.path == null) "" else resource.path
        informationExtractor = InformationExtractor(fileName)
    }


    @Test
    fun getIfConditionBodiesInCameForward() {
    }

    @Test
    fun getConditionAndReturnValueList() {
    }

    @Test
    fun getArgumentTypes() {
    }

    @Test
    fun getParameters() {
    }

    @Test
    fun getCompositeParameters() {
    }

    @Test
    fun setCompositeParameters() {
    }

    @Test
    fun getReturnValue() {
    }

    @Test
    fun getIfElseExprSyntaxTree() {
    }

    @Test
    fun getFunctionName() {
    }

    @Test
    fun getIfConditions() {
    }

    @Test
    fun getVdmFilePath() {
        assertEquals(fileName, informationExtractor.vdmFilePath)
    }
}
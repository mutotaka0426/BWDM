package com.github.korosuke613.bwdm.informationStore

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OperationDefinitionTest {
    private lateinit var operation: OperationDefinition
    private var informationExtractor: InformationExtractor

    init {
        val resource = InformationExtractor::class.java.classLoader.getResource("operation.vdmpp")
        val fileName = if (resource?.path == null) "" else resource.path
        informationExtractor = InformationExtractor(fileName)
        operation = informationExtractor.explicitOperations["うるう年判定"]!!
    }

    @Test
    fun conditionAndReturnValuesTest() {
        val returnStr = arrayListOf("\"うるう年\"", "\"平年\"", "\"うるう年\"", "\"平年\"")
        val conditionNames = arrayListOf("current_year mod rule3 = 0", "current_year mod rule2 = 0", "current_year mod rule1 = 0")
        val conditions = arrayListOf(
                arrayListOf(conditionNames[0], conditionNames[1], conditionNames[2]),
                arrayListOf(conditionNames[0], conditionNames[1], conditionNames[2]),
                arrayListOf(conditionNames[1], conditionNames[2]),
                arrayListOf(conditionNames[2]))
        val boolList = arrayListOf(
                arrayListOf(true, true, true),
                arrayListOf(false, true, true),
                arrayListOf(false, true),
                arrayListOf(false)
        )

        var i = 0
        operation.conditionAndReturnValueList!!.conditionAndReturnValues.forEach { returnValue ->
            assertEquals(returnStr[i], returnValue.returnStr)
            assertEquals(conditions[i], returnValue.conditions)
            assertEquals(boolList[i], returnValue.bools)
            i++
        }
    }

    @Test
    fun ifConditionsModTest() {
        val values = arrayListOf(
                arrayListOf("current_year", "4", "mod"),
                arrayListOf("current_year", "100", "mod"),
                arrayListOf("current_year", "400", "mod")
        )

        var i = 0
        operation.ifConditions["current_year"]!!.forEach { condition ->
            assertEquals(values[i][0], condition["left"])
            assertEquals(values[i][1], condition["right"])
            assertEquals(values[i][2], condition["operator"])
            i++
        }
    }

    @Test
    fun getFunctionName() {
        assertEquals("うるう年判定", operation.operationName)
    }
}
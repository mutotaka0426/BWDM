package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.ast.definitions.ASTDefinition
import com.fujitsu.vdmj.ast.definitions.ASTDefinitionList
import com.fujitsu.vdmj.lex.Dialect
import com.fujitsu.vdmj.lex.LexTokenReader
import com.fujitsu.vdmj.mapper.ClassMapper
import com.fujitsu.vdmj.syntax.DefinitionReader
import com.fujitsu.vdmj.tc.definitions.TCDefinition
import com.fujitsu.vdmj.tc.definitions.TCExplicitOperationDefinition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class OperationDefinitionTest {
    private lateinit var operation: OperationDefinition
    private var astDefinitions: ASTDefinitionList

    init {
        val resource = InformationExtractor::class.java.classLoader.getResource("operation.vdmpp")
        val fileName = if (resource?.path == null) "" else resource.path
        val vdmFile = File(fileName)
        val lexer = LexTokenReader(vdmFile, Dialect.VDM_PP)
        val parser = DefinitionReader(lexer)
        astDefinitions = parser.readDefinitions()
    }

    @BeforeEach
    fun setup() {
        astDefinitions.forEach { astDefinition: ASTDefinition ->
            if (astDefinition.kind() == "explicit function") {
                lateinit var tcOperationDefinition: TCExplicitOperationDefinition
                try {
                    tcOperationDefinition = ClassMapper.getInstance(TCDefinition.MAPPINGS).init().convert<TCExplicitOperationDefinition>(astDefinition)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                operation = OperationDefinition(tcOperationDefinition)
            }
        }
    }

    @Test
    fun conditionAndReturnValuesTest() {
        val returnStr = arrayListOf("\"うるう年\"", "\"平年\"", "\"うるう年\"", "\"平年\"")
        val conditionNames = arrayListOf("年mod400=0", "年mod100=0", "年mod4=0")
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
        operation.conditionAndReturnValueList.conditionAndReturnValues.forEach { returnValue ->
            assertEquals(returnStr[i], returnValue.returnStr)
            assertEquals(conditions[i], returnValue.conditions)
            assertEquals(boolList[i], returnValue.bools)
            i++
        }
    }

    @Test
    fun ifConditionsModTest() {
        val values = arrayListOf(
                arrayListOf("年", "4", "mod"),
                arrayListOf("年", "4", "mod"),
                arrayListOf("年", "100", "mod"),
                arrayListOf("年", "100", "mod"),
                arrayListOf("年", "400", "mod"),
                arrayListOf("年", "400", "mod")
        )

        var i = 0
        operation.ifConditions["年"]!!.forEach { condition ->
            assertEquals(values[i][0], condition["left"])
            assertEquals(values[i][1], condition["right"])
            assertEquals(values[i][2], condition["operator"])
            i++
        }
    }

    @Test
    fun getFunctionName() {
        assertEquals("うるう年判定仕様", operation.operationName)
    }
}
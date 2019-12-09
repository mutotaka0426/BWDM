package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.ast.definitions.ASTDefinition
import com.fujitsu.vdmj.ast.definitions.ASTDefinitionList
import com.fujitsu.vdmj.lex.Dialect
import com.fujitsu.vdmj.lex.LexTokenReader
import com.fujitsu.vdmj.mapper.ClassMapper
import com.fujitsu.vdmj.syntax.DefinitionReader
import com.fujitsu.vdmj.tc.definitions.TCDefinition
import com.fujitsu.vdmj.tc.definitions.TCExplicitFunctionDefinition
import com.fujitsu.vdmj.tc.definitions.TCExplicitOperationDefinition
import com.fujitsu.vdmj.tc.definitions.TCInstanceVariableDefinition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class FunctionDefinitionTest {
    private lateinit var function: FunctionDefinition
    private var astDefinitions: ASTDefinitionList

    init {
        val resource = InformationExtractor::class.java.classLoader.getResource("function.vdmpp")
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
                lateinit var tcFunctionDefinition: TCExplicitFunctionDefinition
                try {
                    tcFunctionDefinition = ClassMapper.getInstance(TCDefinition.MAPPINGS).init().convert<TCExplicitFunctionDefinition>(astDefinition)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                function = FunctionDefinition(tcFunctionDefinition)
            }
        }
    }

    @Test
    fun getFunctionName() {
        assertEquals("うるう年判定仕様", function.functionName)
    }
}
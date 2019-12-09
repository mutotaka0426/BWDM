package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.ast.definitions.ASTDefinition
import com.fujitsu.vdmj.lex.Dialect
import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.lex.LexTokenReader
import com.fujitsu.vdmj.mapper.ClassMapper
import com.fujitsu.vdmj.syntax.DefinitionReader
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCDefinition
import com.fujitsu.vdmj.tc.definitions.TCExplicitFunctionDefinition
import org.junit.jupiter.api.*
import java.io.File
import java.util.function.Consumer

internal class IfElseExprSyntaxTreeTest {
    @BeforeEach
    fun init() {
    }

    @Test
    fun ほげ() {
        assert(IfElseExprSyntaxTree::class.java.isInstance(ifElseExprSyntaxTree))
    }

    @AfterEach
    fun tearDown() {
    }

    companion object {
        private var ifElseExprSyntaxTree: IfElseExprSyntaxTree? = null
        @BeforeAll
        @Throws(LexException::class, ParserException::class)
        fun initAll() {
            val lexer = LexTokenReader(
                    File("./vdm_files/Arg2_Japanese.vdmpp"), Dialect.VDM_PP)
            val parser = DefinitionReader(lexer)
            val astDefinitions = parser.readDefinitions()
            astDefinitions.forEach(Consumer { astDefinition: ASTDefinition ->
                if (astDefinition.kind() == "explicit function") {
                    var tcFunctionDefinition: TCExplicitFunctionDefinition? = null
                    try {
                        tcFunctionDefinition = ClassMapper.getInstance(TCDefinition.MAPPINGS)
                                .init()
                                .convert<TCExplicitFunctionDefinition>(astDefinition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    assert(tcFunctionDefinition != null)
                    val tcExpression = tcFunctionDefinition!!.body
                    val ifExpressionBody = tcExpression.toString()
                    ifElseExprSyntaxTree = null
                    ifElseExprSyntaxTree = IfElseExprSyntaxTree(ifExpressionBody)
                }
            })
        }

        @AfterAll
        fun tearDownAll() {
        }
    }
}
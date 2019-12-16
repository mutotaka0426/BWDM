package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCExplicitFunctionDefinition
import java.io.IOException

class FunctionDefinition
(tcDefinition: TCExplicitFunctionDefinition) : Definition(tcDefinition) {
    override var returnValue: String = tcDefinition.type.result.toString()

    init {
        // 引数の型を登録
        tcDefinition.type.parameters.forEach { e -> argumentTypes.add(e.toString()) }

        // IfElseを構文解析
        ifExpressionBody = tcDefinition.body.toString()

        try {
            ifElseExprSyntaxTree = IfElseExprSyntaxTree(ifExpressionBody)
        } catch (e: ParserException) {
            e.printStackTrace()
        } catch (e: LexException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        setIfElseSyntaxTree()
    }

    override fun setIfElseSyntaxTree() {
        //parsing for parameters
        val tcPatternList = (tcDefinition as TCExplicitFunctionDefinition).paramPatternList[0]  // 仮引数
        for (parameter in tcPatternList) {
            parameters.add(parameter.toString())
        }

        parseIfConditions()
        ifElseExprSyntaxTree = IfElseExprSyntaxTree(ifExpressionBody)
        conditionAndReturnValueList = ConditionAndReturnValueList(ifElseExprSyntaxTree!!.root)
    }

    override fun parseIfConditions() {
        val ifElses = ifElseExprSyntaxTree!!.ifElses

        compositeParameters = ArrayList(parameters)

        for (i in ifElses.indices) {
            var element = ifElses[i]
            if (element == "if") {
                element = ifElses[i + 1]
                createCompositParameters(element)
            }
        }

        initializeIfconditions()
        createIfCondition()
    }
}

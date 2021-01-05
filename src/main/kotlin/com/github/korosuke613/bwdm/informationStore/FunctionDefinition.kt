package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCExplicitFunctionDefinition
import com.fujitsu.vdmj.tc.definitions.TCValueDefinition
import com.fujitsu.vdmj.tc.definitions.TCTypeDefinition
import java.io.IOException
import com.github.korosuke613.bwdm.boundaryValueAnalysisUnit.ConditionAnalyzer

class FunctionDefinition
(tcDefinition: TCExplicitFunctionDefinition,
 private val constantValues: LinkedHashMap<String, TCValueDefinition>, private val types: LinkedHashMap<String, TCTypeDefinition>) : Definition(tcDefinition) {
    override var returnValue: String = tcDefinition.type.result.toString()

    init {
        // 引数の型を登録
        tcDefinition.type.parameters.forEach { e ->
			var argumentType = e.toString()
			argumentType = argumentType.removePrefix("(unresolved ")
			argumentType = argumentType.removeSuffix(")")
			argumentTypes.add(argumentType)
		}


        // IfElseを構文解析
        ifExpressionBody = tcDefinition.body.toString()

        try {
            // 定数を実数に置き換え
            constantValues.forEach {
                if (ifExpressionBody.contains(it.key)) {
                    ifExpressionBody = ifExpressionBody.replace(it.key, it.value.exp.toString())
                }
            }
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

		setTypeInvariant()
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

                // 定数を実数に置き換え
                constantValues.forEach {
                    if (element.contains(it.key)) {
                        element = element.replace(it.key, it.value.exp.toString())
                        ifElses[i + 1] = element
                    }
                }
                createCompositParameters(element)
            }
        }

        initializeIfconditions()
        createIfCondition()
    }

	override fun setTypeInvariant() {
		for(i in parameters.indices) {
			var parameter = parameters[i]
			var argumentType = argumentTypes[i]
			typeInvariants.add("")
			types.forEach { 
				if(it.key == argumentType) {
					argumentTypes[i] = it.value.type.toDetailedString()

					var type = it.value
					// 型の不変条件式をセット
					if(type.invPattern != null){
						var expression = type.invExpression.toString()
						expression = expression.replace(type.invPattern.toString(), parameter)
						expression = ConditionAnalyzer.summarizeExpression(expression)

						typeInvariants[i] = expression
                		createCompositParameters(expression)
					}
				}
			}
		}
	}
}

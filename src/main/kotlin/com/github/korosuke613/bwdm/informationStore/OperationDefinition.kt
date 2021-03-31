package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCExplicitOperationDefinition
import com.fujitsu.vdmj.tc.definitions.TCInstanceVariableDefinition
import com.fujitsu.vdmj.tc.definitions.TCValueDefinition
import com.fujitsu.vdmj.tc.definitions.TCTypeDefinition
import java.io.IOException
import com.github.korosuke613.bwdm.boundaryValueAnalysisUnit.BoundaryValueAnalyzer
import com.github.korosuke613.bwdm.ConditionAnalyzer
import com.github.korosuke613.bwdm.replaceVariable
import com.github.korosuke613.bwdm.searchVariable
import com.github.korosuke613.bwdm.alignParentheses

class OperationDefinition
(tcDefinition: TCExplicitOperationDefinition,
 private val instanceVariables: LinkedHashMap<String, TCInstanceVariableDefinition>,
 private val constantValues: LinkedHashMap<String, TCValueDefinition>,
 private val types: LinkedHashMap<String, TCTypeDefinition>,
 private val invariant: String) : Definition(tcDefinition) {

    // 利用しているメンバ変数のリスト
    private val usedInstanceVariables: LinkedHashMap<String, String> = linkedMapOf()

    override var returnValue: String = tcDefinition.type.result.toString()

	private	val objectState = ObjectState(tcDefinition)
	
	// 操作の被代入変数(key)と代入演算式(value)
	val operationExpressions: LinkedHashMap<String, String> = linkedMapOf()

	// インスタンス変数(key)と操作前の値(value)
	val instanceExpressions: LinkedHashMap<String, String> = linkedMapOf()

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
                    ifExpressionBody = ifExpressionBody.replaceVariable(it.key, it.value.exp.toString())
                }
            }
            ifElseExprSyntaxTree = IfElseExprSyntaxTree(ifExpressionBody)
        } catch (e: NotIfNodeException) {
            isSetter = true
        } catch (e: ParserException) {
            e.printStackTrace()
        } catch (e: LexException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (!isSetter) {
            setIfElseSyntaxTree()
        }

		// オブジェクトを操作していなければfalseにする
		isObjectSetter = objectState.isConstant
		if (isSetter && !isObjectSetter) {
			setIfElseSyntaxTree()
			setObjectState()
		}
    }

    override fun setIfElseSyntaxTree() {
        //parsing for parameters
        if (isObjectSetter) setUsedInstanceVariables()

    	val tcPatternList = (tcDefinition as TCExplicitOperationDefinition).paramPatternList[0] // 仮引数
    	for (parameter in tcPatternList) {
    	    parameters.add(parameter.toString())
    	}
    	for (variable in usedInstanceVariables) {
    	    parameters.add(variable.key)
    	    argumentTypes.add(variable.value)
    	}
	
		setInputConditions()
        parseIfConditions()

        if (isObjectSetter) conditionAndReturnValueList = ConditionAndReturnValueList(ifElseExprSyntaxTree!!.root)
    }

    private fun setUsedInstanceVariables() {
        instanceVariables.forEach {
            if (ifExpressionBody.contains(it.key)) {
				var instanceVariableType = it.value.type.toString()

				// 型定義を使用してる場合実際の型に変換する
				if(instanceVariableType.startsWith("(unresolved ")) {
					instanceVariableType = instanceVariableType.removePrefix("(unresolved ")
					instanceVariableType = instanceVariableType.removeSuffix(")")
					run {
					    types.forEach { t ->
							if(t.key == instanceVariableType) {
								if(t.value.invPattern != null){
									var expression = t.value.invExpression.toString()
									expression = expression.replaceVariable(t.value.invPattern.toString(), it.key)
									expression = ConditionAnalyzer.expressionTree(expression)
									addCondition(expression, true)
            			    		createCompositParameters(expression)
								}

							    instanceVariableType = t.value.type.toDetailedString()
								return@run
							}
						}
					}
				} 
                usedInstanceVariables[it.key] = instanceVariableType 
			}
        }
    }

    override fun parseIfConditions() {

		if(isObjectSetter) {
        	val ifElses = ifElseExprSyntaxTree!!.ifElses


        	for (i in ifElses.indices) {
        	    var element = ifElses[i]
        	    if (element == "if") {
        	        element = ifElses[i + 1]

        	        // 定数を実数に置き換え
        	        constantValues.forEach {
        	            if (element.contains(it.key)) {
        	                element = element.replaceVariable(it.key, it.value.exp.toString())
        	                ifElses[i + 1] = element
        	            }
        	        }
        	        createCompositParameters(element)
        	    }
        	}
		}

        initializeIfconditions()
        createIfCondition()
    }

	override fun setInputConditions() {
       	compositeParameters = ArrayList(parameters)
		// 事前条件式をセット
		if(objectState.precondition != ""){	
			var precondition = ConditionAnalyzer.expressionTree(objectState.precondition)
			addCondition(precondition, true)
			precondition.split(" and ", " or ").forEach {
				createCompositParameters(it.alignParentheses().removePrefix("(").removeSuffix(")"))
			}
		}

		for(i in parameters.indices) {
			val parameter = parameters[i]
			val argumentType = argumentTypes[i]

			types.forEach { 
				if(it.key == argumentType) {
					argumentTypes[i] = it.value.type.toDetailedString()

					var type = it.value
					// 型の不変条件式をセット
					if(type.invPattern != null){
						var expression = type.invExpression.toString()
						expression = expression.replaceVariable(type.invPattern.toString(), parameter)
						expression = ConditionAnalyzer.expressionTree(expression)

						addCondition(expression, true)

						expression.split(" and ", " or ").forEach {
							// 括弧を揃えて一番外側の括弧を外す
							val exp = it.alignParentheses().removePrefix("(").removeSuffix(")")
                			createCompositParameters(exp)
						}
					}
				}
			}
		}
	}

	fun setObjectState() {
		// 操作するインスタンス変数名をkeyにして操作の演算式を格納する
		objectState.expressions.forEach {
			val operator = it.indexOf(" := ")
			val name = it.substring(0, operator)
			val expression = it.substring(operator + 4)

			operationExpressions[name] = expression
		}

		// 事後条件をセット
		if(objectState.postcondition != ""){
			addCondition(objectState.postcondition, false)
		}
		// クラス不変条件をセット
		if(invariant != "") {
			addCondition(invariant, false)
		}

		// 演算処理で使用するインスタンス変数を格納する
		instanceVariables.forEach { variable ->
			run {
				operationExpressions.forEach { expression ->
					if(expression.value.searchVariable(variable.key)){
						// 操作前のインスタンス変数の値をセット
						instanceExpressions[variable.key] = variable.value.expression.toString()

						var currentTyp = variable.value.type.toString()
						if(currentTyp.startsWith("(unresolved ")) {	// 型定義の場合
							currentTyp = currentTyp.removePrefix("(unresolved ").removeSuffix(")")
							// actualType [実際の型]
							val actualType = types[currentTyp]?.type?.toDetailedString()
							generateTypeBoundary(variable.key, actualType.toString())
						}
						else {
							generateTypeBoundary(variable.key, currentTyp)
						}
						return@run
					}
				}
			}
		}
	}

	/*
	 * 定数とインスタンス変数を実際の値に置換してから
	 * inputConditions or outputConditionsに追加する
	 */ 
	private fun addCondition(_condition: String, toInputConditions: Boolean) {
		var condition = _condition
		
		constantValues.forEach {
			condition = condition.replaceVariable(it.key, it.value.exp.toString())
		}

		if(toInputConditions) {
			inputConditions.add(condition)
		} else {
			outputConditions.add(condition)
		}
	}

	private fun generateTypeBoundary(name: String, type: String) {
		when(type) {
			"int" -> {
				outputConditions.add("$name <= ${BoundaryValueAnalyzer.intMax}")
				outputConditions.add("$name >= ${BoundaryValueAnalyzer.intMin}")
			}
			"nat" -> {
				outputConditions.add("$name <= ${BoundaryValueAnalyzer.natMax}")
				outputConditions.add("$name >= ${BoundaryValueAnalyzer.natMin}")
			}
			"nat1" -> {
				outputConditions.add("$name <= ${BoundaryValueAnalyzer.nat1Max}")
				outputConditions.add("$name >= ${BoundaryValueAnalyzer.nat1Min}")
			}
		}
	}
}

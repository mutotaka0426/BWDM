package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.tc.definitions.TCDefinition
import com.github.korosuke613.bwdm.Util

abstract class Definition(val tcDefinition: TCDefinition) {
    private val ifConditionBodiesInCameForward: ArrayList<String> = ArrayList()

    // +でつながった変数の式
    var compositeParameters: ArrayList<String> = arrayListOf()

    protected var ifExpressionBody: String = ""

    var conditionAndReturnValueList: ConditionAndReturnValueList? = null

    /**
     * type of return value
     */
    abstract var returnValue: String

    /**
     * a parameter to ArrayList of HashMaps that is parsed each if-expression.
     * ArrayList of HashMap of parsed if-expr.
     */
    val ifConditions: HashMap<String, ArrayList<HashMap<String, String>>> = HashMap()

    var name: String = tcDefinition.name.name

    // 仮引数のリスト
    val parameters: ArrayList<String> = ArrayList()

    // 引数の型リスト
    val argumentTypes: ArrayList<String> = ArrayList()

	// 入力値の条件式リスト(型の不変条件と事前条件)
	val inputConditions: ArrayList<String> = ArrayList()

	// 期待値の条件式リスト(クラス不変条件と事後条件)
	val outputConditions: ArrayList<String> = ArrayList()
	
    var ifElseExprSyntaxTree: IfElseExprSyntaxTree? = null
        protected set

    var isSetter: Boolean = false

    var isObjectSetter: Boolean = true 

    abstract fun setIfElseSyntaxTree()
    abstract fun parseIfConditions()
    abstract fun setInputConditions()

    protected fun createCompositParameters(element: String) {
        ifConditionBodiesInCameForward.add(element)
        val operator = Util.getOperator(element)
        val indexOfOperator = element.indexOf(operator)
        val left = element.substring(0, indexOfOperator)

        // 左辺に+が含まれていたらそれも仮引数リストに加え入れる。
        if (Util.getOperator(left) == "+") {
            compositeParameters.add(left)
        }
    }

    protected fun initializeIfconditions() {
        //initializing of collection instances of each parameter
        compositeParameters.forEach { s ->
            ifConditions[s] = ArrayList()
        }
    }

    protected fun createIfCondition() {
        fun addIfConditions(condition: String, parameter: String) {
            val operator = Util.getOperator(condition)
            val indexOfOperator = condition.indexOf(operator)
            val hm = HashMap<String, String>()
            hm["left"] = condition.substring(0, indexOfOperator).replace(" ", "")
            hm["operator"] = operator

            //right-hand and surplus need branch depending on mod or other.
            modJudge(condition, operator, indexOfOperator, hm)
            ifConditions[parameter]!!.add(hm)
        }

        //parsing of each if-condition, and store in ifConditions
        ifConditionBodiesInCameForward.forEach { condition ->
            val operator = Util.getOperator(condition)
            val indexOfOperator = condition.indexOf(operator)
            val left = condition.substring(0, indexOfOperator)
            compositeParameters.forEach { parameter ->
                if (left == parameter) {
                    addIfConditions(condition, parameter)
                }
            }
            parameters.forEach { parameter ->
                if (condition.contains(parameter)) {
                    addIfConditions(condition, parameter)
                }
            }
        }
    }

    companion object {
        fun modJudge(condition: String, operator: String, indexOfOperator: Int, hm: HashMap<String, String>) {
            if (operator == "mod") {
                val indexOfEqual = condition.indexOf("=")
				hm["left"] = hm["left"]!!.replace("(", "")
                hm["right"] = condition.substring(indexOfOperator + 3, indexOfEqual).replace(" ", "").replace(")", "")
                hm["surplus"] = condition.substring(indexOfEqual + 1).replace(" ", "").replace(")", "")
            } else {
                hm["right"] = condition.substring(indexOfOperator + operator.length).replace(" ", "")
            }
        }
    }
}

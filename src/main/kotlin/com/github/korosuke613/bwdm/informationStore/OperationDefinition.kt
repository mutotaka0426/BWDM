package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCExplicitOperationDefinition
import com.fujitsu.vdmj.tc.definitions.TCInstanceVariableDefinition
import com.fujitsu.vdmj.tc.definitions.TCValueDefinition
import com.github.korosuke613.bwdm.Util
import java.io.IOException

class OperationDefinition
constructor(private val tcOperationDefinition: TCExplicitOperationDefinition,
            private val instanceVariables: LinkedHashMap<String, TCInstanceVariableDefinition>,
            private val constantValues: LinkedHashMap<String, TCValueDefinition>) {
    private val ifConditionBodiesInCameForward: ArrayList<String> = ArrayList()

    // +でつながった変数の式
    var compositeParameters: ArrayList<String> = arrayListOf()


    private var ifExpressionBody: String = ""
    var conditionAndReturnValueList: ConditionAndReturnValueList? = null

    // 仮引数のリスト
    val parameters: ArrayList<String> = ArrayList()

    // 引数の型リスト
    val argumentTypes: ArrayList<String> = ArrayList()

    // 利用しているメンバ変数のリスト
    val usedInstanceVariables: ArrayList<String> = ArrayList()

    /**
     * a parameter to ArrayList of HashMaps that is parsed each if-expression.
     * ArrayList of HashMap of parsed if-expr.
     */
    val ifConditions: HashMap<String, ArrayList<HashMap<String, String>>> = HashMap()

    /**
     * type of return value
     */
    var returnValue: String = tcOperationDefinition.type.result.toString()
        private set

    var ifElseExprSyntaxTree: IfElseExprSyntaxTree? = null
        private set

    var operationName: String = tcOperationDefinition.name.name
        private set

    var isSetter: Boolean = false

    init {
        // 引数の型を登録
        val tcOperationType = tcOperationDefinition.type
        tcOperationType.parameters.forEach { e -> argumentTypes.add(e.toString()) }

        // IfElseを構文解析
        val tcExpression = tcOperationDefinition.body
        ifExpressionBody = tcExpression.toString()

        try {
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
            //parsing for parameters
            val tcPatternList = tcOperationDefinition.paramPatternList[0]  // 仮引数
            setUsedInstanceVariables()

            for (parameter in tcPatternList) {
                parameters.add(parameter.toString())
            }
            for (variable in usedInstanceVariables) {
                parameters.add(variable)
            }

            parseIfConditions()

            ifElseExprSyntaxTree = IfElseExprSyntaxTree(ifExpressionBody)
            conditionAndReturnValueList = ConditionAndReturnValueList(ifElseExprSyntaxTree!!.root)
        }
    }

    private fun setUsedInstanceVariables() {
        instanceVariables.forEach {
            if (ifExpressionBody.contains(it.key)) {
                usedInstanceVariables.add(it.key)
            }
        }
    }

    private fun parseIfConditions() {
        val ifElses = ifElseExprSyntaxTree!!.ifElses

        compositeParameters = ArrayList(parameters)

        for (i in ifElses.indices) {
            var element = ifElses[i]
            if (element == "if") {
                element = ifElses[i + 1]
                constantValues.forEach {
                    if (element.contains(it.key)) {
                        element = element.replace(it.key, it.value.exp.toString())
                    }
                }
                ifConditionBodiesInCameForward.add(element)
                val operator = Util.getOperator(element)
                val indexOfOperator = element.indexOf(operator)
                val left = element.substring(0, indexOfOperator)

                // 左辺に+が含まれていたらそれも仮引数リストに加え入れる。
                if (Util.getOperator(left) == "+") {
                    compositeParameters.add(left)
                }
            }
        }

        //initializing of collection instances of each parameter
        compositeParameters.forEach { s ->
            ifConditions[s] = ArrayList()
        }

        //parsing of each if-condition, and store in ifConditions
        ifConditionBodiesInCameForward.forEach { condition ->
            val operator = Util.getOperator(condition)
            val indexOfOperator = condition.indexOf(operator)
            val left = condition.substring(0, indexOfOperator)
            compositeParameters.forEach { parameter ->
                if (left == parameter) {
                    parse(condition, parameter)
                }
            }
            parameters.forEach { parameter ->
                if (condition.contains(parameter)) {
                    parse(condition, parameter)
                }
            }
        }
    }

    private fun parse(condition: String, parameter: String) {
        val operator = Util.getOperator(condition)
        val indexOfOperator = condition.indexOf(operator)
        val hm = HashMap<String, String>()
        hm["left"] = condition.substring(0, indexOfOperator).replace(" ", "")
        hm["operator"] = operator

        //right-hand and surplus need branch depending on mod or other.
        modJudge(condition, operator, indexOfOperator, hm)
        val al = ifConditions[parameter]
        al!!.add(hm)
    }

    companion object {
        fun modJudge(condition: String, operator: String, indexOfOperator: Int, hm: HashMap<String, String>) {
            if (operator == "mod") {
                val indexOfEqual = condition.indexOf("=")
                hm["right"] = condition.substring(indexOfOperator + 3, indexOfEqual).replace(" ", "")
                hm["surplus"] = condition.substring(indexOfEqual + 1).replace(" ", "")
            } else {
                hm["right"] = condition.substring(indexOfOperator + operator.length).replace(" ", "")
            }
        }
    }
}
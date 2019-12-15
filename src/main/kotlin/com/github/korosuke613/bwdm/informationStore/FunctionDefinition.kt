package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCExplicitFunctionDefinition
import com.fujitsu.vdmj.tc.patterns.TCIdentifierPattern
import com.github.korosuke613.bwdm.Util
import java.io.IOException

class FunctionDefinition
constructor(private val tcFunctionDefinition: TCExplicitFunctionDefinition) {
    private val ifConditionBodiesInCameForward: ArrayList<String> = ArrayList()

    // +でつながった変数の式
    lateinit var compositeParameters: ArrayList<String>


    private var ifExpressionBody: String = ""
    var conditionAndReturnValueList: ConditionAndReturnValueList

    // 仮引数のリスト
    val parameters: ArrayList<String> = ArrayList()

    // 引数の型リスト
    val argumentTypes: ArrayList<String> = ArrayList()

    /**
     * a parameter to ArrayList of HashMaps that is parsed each if-expression.
     * ArrayList of HashMap of parsed if-expr.
     */
    val ifConditions: HashMap<String, ArrayList<HashMap<String, String>>> = HashMap()

    /**
     * type of return value
     */
    var returnValue: String = tcFunctionDefinition.type.result.toString()
        private set

    var ifElseExprSyntaxTree: IfElseExprSyntaxTree? = null
        private set

    var functionName: String = tcFunctionDefinition.name.name
        private set

    init {
        // 引数の型を登録
        val tcFunctionType = tcFunctionDefinition.type
        tcFunctionType.parameters.forEach { e -> argumentTypes.add(e.toString()) }

        // IfElseを構文解析
        val tcExpression = tcFunctionDefinition.body
        ifExpressionBody = tcExpression.toString()

        try {
            ifElseExprSyntaxTree = IfElseExprSyntaxTree(ifExpressionBody)
        } catch (e: ParserException) {
            e.printStackTrace()
        } catch (e: LexException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //parsing for parameters
        val tcPatternList = tcFunctionDefinition.paramPatternList[0]  // 仮引数
        for (parameter in tcPatternList) {
            parameters.add(parameter.toString())
        }


        parseIfConditions()

        ifElseExprSyntaxTree = IfElseExprSyntaxTree(ifExpressionBody)
        conditionAndReturnValueList = ConditionAndReturnValueList(ifElseExprSyntaxTree!!.root)
    }

    private fun parseIfConditions() {
        val ifElses = ifElseExprSyntaxTree!!.ifElses

        compositeParameters = ArrayList(parameters)

        for (i in ifElses.indices) {
            var element = ifElses[i]
            if (element == "if") {
                element = ifElses[i + 1]
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
                if(condition.contains(parameter)){
                    parse(condition, parameter)
                }
            }
        }
    }

    private fun parse(condition: String, parameter: String) {
        val operator = Util.getOperator(condition)
        val indexOfOperator = condition.indexOf(operator)
        val hm = HashMap<String, String>()
        hm["left"] = condition.substring(0, indexOfOperator)
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
                hm["right"] = condition.substring(indexOfOperator + 3, indexOfEqual)
                hm["surplus"] = condition.substring(indexOfEqual + 1)
            } else {
                hm["right"] = condition.substring(indexOfOperator + operator.length)
            }
        }
    }
}
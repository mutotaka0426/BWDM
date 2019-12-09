package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCExplicitFunctionDefinition
import com.fujitsu.vdmj.tc.patterns.TCIdentifierPattern
import com.github.korosuke613.bwdm.Util
import java.io.IOException

class Function
constructor(tcFunctionDefinition: TCExplicitFunctionDefinition) {
    private val ifConditionBodiesInCameForward: ArrayList<String> = ArrayList()
    private lateinit var compositeParameters: ArrayList<String>
    private var ifExpressionBody: String = ""
    private var conditionAndReturnValueList: ConditionAndReturnValueList
    val parameters: ArrayList<String> = ArrayList()
    private val argumentTypes: ArrayList<String> = ArrayList()

    /**
     * a parameter to ArrayList of HashMaps that is parsed each if-expression.
     * ArrayList of HashMap of parsed if-expr.
     */
    private val ifConditions: HashMap<String, ArrayList<HashMap<String, String>>> = HashMap()

    /**
     * type of return value
     */
    var returnValue: String? = null
        private set

    var ifElseExprSyntaxTree: IfElseExprSyntaxTree? = null
        private set

    var functionName: String
        private set

    init {
        functionName = tcFunctionDefinition.name.name
        returnValue = tcFunctionDefinition.type.result.toString()
        val tcFunctionType = tcFunctionDefinition.type
        val tcExpression = tcFunctionDefinition.body
        ifExpressionBody = tcExpression.toString()
        tcFunctionType.parameters.forEach { e -> argumentTypes.add(e.toString()) }

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
        val tcPatternListList = tcFunctionDefinition.paramPatternList
        val tcPatternList = tcPatternListList.firstElement()
        for (aTcPatternList in tcPatternList) {
            val tcIdentifierPattern = aTcPatternList as TCIdentifierPattern
            val parameter = tcIdentifierPattern.toString()
            parameters.add(parameter)
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
                val indexOfoperator = element.indexOf(operator)
                val left = element.substring(0, indexOfoperator)
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
            val indexOfoperator = condition.indexOf(operator)
            val left = condition.substring(0, indexOfoperator)
            compositeParameters.forEach { parameter ->
                if (left == parameter) {
                    parse(condition, parameter)
                }
            }
        }
    }

    private fun parse(condition: String, parameter: String) {
        val operator = Util.getOperator(condition)
        val indexOfoperator = condition.indexOf(operator)
        val hm = HashMap<String, String>()
        hm["left"] = condition.substring(0, indexOfoperator)
        hm["operator"] = operator

        val al = ifConditions[parameter]
        al!!.add(hm)
        //right-hand and surplus need branch depending on mod or other.
        modJudge(condition, operator, indexOfoperator, hm)

    }

    companion object {
        fun modJudge(condition: String, operator: String, indexOfoperator: Int, hm: HashMap<String, String>) {
            if (operator == "mod") {
                val indexOfEqual = condition.indexOf("=")
                hm["right"] = condition.substring(indexOfoperator + 3, indexOfEqual)
                hm["surplus"] = condition.substring(indexOfEqual + 1)
            } else {
                hm["right"] = condition.substring(indexOfoperator + operator.length)
            }
        }
    }
}
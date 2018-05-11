package bwdm.boundaryValueAnalysisUnit

import bwdm.informationStore.InformationExtractor
import bwdm.informationStore.Node
import bwdm.informationStore.IfNode
import bwdm.Util

import java.util.ArrayList
import java.util.HashMap
import java.util.Objects

typealias Expression = HashMap<String, String>

class ExpectedOutputDataGenerator internal constructor(private val ie: InformationExtractor,
                                                       _root: IfNode,
                                                       _inputDataList: ArrayList<HashMap<String, Long>>) {
    internal val expectedOutputDataList: ArrayList<String> = ArrayList()

    init {

        //inputData:seq of HashMapの各要素に入力データを挿入
        _inputDataList.forEach { inputData -> extractExpectedOutputDataRecursively(_root, inputData) }

        //各要素が型の範囲外の値だった場合、Undefined Actionを期待出力にする
        makeExpectedOutputDataofOutoftype(_inputDataList)


    }

    //条件式は両辺のうち片方のみ変数が含まれているという制約付き
    private fun extractExpectedOutputDataRecursively(_node: Node, _inputData: HashMap<String, Long>) {

        if (_node.getIsIfNode()) { //IfNodeである場合
            val parsedCondition = makeParsedCondition(_node.conditionOrReturnStr)

            //各条件式には一つの変数(parameter)しか登場しない
            ie.parameters.forEach { prm ->
                //条件式中の変数とprmが一致したらinputDataを代入して真偽判定
                if (parsedCondition["right"] == prm || parsedCondition["left"] == prm) {
                    val conditionJudgeResult = judge(parsedCondition, _inputData, prm)

                    //判定結果がTRUEならばifNodeのtrueNodeに進んで再帰
                    if (conditionJudgeResult) {
                        extractExpectedOutputDataRecursively(Objects.requireNonNull<Node>((_node as IfNode).conditionTrueNode), _inputData)
                    } else { //判定結果がFalseならばifNodeのfalseNodeに進んで再帰
                        extractExpectedOutputDataRecursively(
                                Objects.requireNonNull<Node>((_node as IfNode).conditionFalseNode),
                                _inputData
                        )
                    }
                }
            }
        } else { //returnNodeである場合
            val returnStr = _node.conditionOrReturnStr
            expectedOutputDataList.add(returnStr)
        }

    }

    private fun judge(_parsedCondition: Expression,
                      _inputData: HashMap<String, Long>,
                      _parameter: String): Boolean {
        val result: Boolean

        if (Util.isNumber(_parsedCondition["left"]!!)) { //右辺が変数
            result = if (_parsedCondition["operator"] == "mod") {
                judgeMod(
                        java.lang.Long.valueOf(_parsedCondition["left"]), //左辺：数字
                        _inputData[_parameter], //右辺：変数
                        java.lang.Long.valueOf(_parsedCondition["surplus"])
                )
            } else {
                judgeInequality(
                        java.lang.Long.valueOf(_parsedCondition["left"]), //左辺：数字
                        _parsedCondition["operator"]!!,
                        _inputData[_parameter] //右辺：変数
                )
            }
        } else { //左辺が変数
            result = if (_parsedCondition["operator"] == "mod") {
                judgeMod(
                        _inputData[_parameter], //左辺：変数
                        java.lang.Long.valueOf(_parsedCondition["right"]), //右辺：数字
                        java.lang.Long.valueOf(_parsedCondition["surplus"])
                )
            } else {
                judgeInequality(
                        _inputData[_parameter], //左辺：変数
                        _parsedCondition["operator"]!!,
                        java.lang.Long.valueOf(_parsedCondition["right"])//右辺：数字
                )
            }

        }

        return result
    }

    private fun judgeMod(_leftHand: Long?, _rightHand: Long?, _surplus: Long?): Boolean {
        return _leftHand!! % _rightHand!! == _surplus
    }

    private fun judgeInequality(_leftHand: Long?, _operator: String, _rightHand: Long?): Boolean {
        return when (_operator) {
            "<" -> _leftHand!! < _rightHand!!
            ">" -> _leftHand!! > _rightHand!!
            "<=" -> _leftHand!! <= _rightHand!!
            ">=" -> _leftHand!! >= _rightHand!!
            else -> true
        }
    }


    private fun makeExpectedOutputDataofOutoftype(_inputDataList: ArrayList<HashMap<String, Long>>) {

        for (i in _inputDataList.indices) {
            val currentInputData = _inputDataList[i]

            for (j in 0 until ie.parameters.size) {
                val currentTyp = ie.argumentTypes[j]
                val currentPrm = ie.parameters[j]
                val currentVl = currentInputData[currentPrm]

                when (currentTyp) {
                    "int" -> if (currentVl == BoundaryValueAnalyzer.intMax + 1 || currentVl == BoundaryValueAnalyzer.intMin - 1) {
                        expectedOutputDataList[i] = "Undefined Action"
                    }
                    "nat" -> if (currentVl == BoundaryValueAnalyzer.natMax + 1 || currentVl == BoundaryValueAnalyzer.natMin - 1) {
                        expectedOutputDataList[i] = "Undefined Action"
                    }
                    "nat1" -> if (currentVl == BoundaryValueAnalyzer.nat1Max + 1 || currentVl == BoundaryValueAnalyzer.nat1Min - 1) {
                        expectedOutputDataList[i] = "Undefined Action"
                    }
                }//nothing to do

            }
        }

    }

    companion object {

        fun makeParsedCondition(_condition: String): Expression {
            val parsedCondition = Expression()
            val operator = Util.getOperator(_condition)
            val indexOfoperator = _condition.indexOf(operator)

            parsedCondition["left"] = _condition.substring(0, indexOfoperator)
            parsedCondition["operator"] = operator
            InformationExtractor.modJudge(_condition, operator, indexOfoperator, parsedCondition)
            return parsedCondition
        }
    }
}
package com.github.korosuke613.bwdm.domainAnalysis

import com.github.korosuke613.bwdm.Util
import com.github.korosuke613.bwdm.boundaryValueAnalysisUnit.ExpectedOutputDataGenerator
import com.github.korosuke613.bwdm.informationStore.ConditionAndReturnValueList
import com.github.korosuke613.bwdm.informationStore.FunctionDefinition
import com.github.korosuke613.bwdm.informationStore.IfElseExprSyntaxTree
import com.github.korosuke613.bwdm.informationStore.InformationExtractor
import com.microsoft.z3.ArithExpr
import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Status
import java.util.*
import kotlin.collections.ArrayList

class DomainAnalyser(private val functionDefinition: FunctionDefinition){
    val domains: ArrayList<DomainPoints> = ArrayList()
    val inputDataList: ArrayList<HashMap<String, Long>> = ArrayList()
    val expectedOutputDataGenerator: ExpectedOutputDataGenerator
    var expectedCount = 0

    private val ctx: Context = Context()

    init {
        for(condition in functionDefinition.conditionAndReturnValueList.conditionAndReturnValues) {
            val domainPoints = DomainPoints(condition.returnStr.toString())
            generateInPoints(domainPoints, condition)
            generateOnPoints(domainPoints, condition)
            generateOffPoints(domainPoints)
            generateOutPoints(domainPoints, condition)
            domains.add(domainPoints)
        }
        createInputDataList()
        expectedOutputDataGenerator = ExpectedOutputDataGenerator(
                functionDefinition,
                Objects.requireNonNull<IfElseExprSyntaxTree>(functionDefinition.ifElseExprSyntaxTree).root,
                inputDataList
        )
    }

    private fun createInputDataList(){
        for(dp in domains){
            for((k, p) in dp.onPoints.toSortedMap()){
                inputDataList.add(p.inputData)
            }
            for((k, p) in dp.offPoints.toSortedMap()){
                inputDataList.add(p.inputData)
            }
            for((k, p) in dp.inPoints.toSortedMap()){
                inputDataList.add(p.inputData)
            }
            for((k, p) in dp.outPoints.toSortedMap()){
                inputDataList.add(p.inputData)
            }
        }
    }

    val allTestcasesByDa: String
        get() {
            val buf = StringBuilder()
            expectedCount = 0
            for(dp in domains) {
                buf.append("- ${dp.name}\n")
                addResultBuf(buf, dp.onPoints, "Onポイント")
                addResultBuf(buf, dp.offPoints, "Offポイント")
                addResultBuf(buf, dp.inPoints, "Inポイント")
                addResultBuf(buf, dp.outPoints, "Outポイント")
                buf.append("\n")
            }
            return buf.toString()
        }

    private fun addResultBuf(buf: StringBuilder, points: HashMap<String, Point>, title: String){
        var i = 0
        buf.append("-- $title\n")
        for ((k, p) in points.toSortedMap()) {
            buf.append("No.").append(i + 1).append(" : ")
            for (prm in functionDefinition.parameters) {
                buf.append(p.factors[prm]).append(" ")
            }
            buf.append("-> ").append(expectedOutputDataGenerator.expectedOutputDataList[expectedCount]).append("\n")
            expectedCount++
            i++
        }
    }

    private fun generateOffPoints(dp: DomainPoints) {
        for ((k, p) in dp.onPoints) {
            if(Util.getOperator(p.type) == "+"){
                for((k_, f) in p.factors) {
                    copyOffPoint(dp, p, k_, 1)
                    copyOffPoint(dp, p, k_, -1)
                }
            }else{
                copyOffPoint(dp, p, p.type, 1)
                copyOffPoint(dp, p, p.type, -1)
            }
        }
    }

    private fun copyOffPoint(dp: DomainPoints, point: Point, key: String, add: Int = 0){
        val f: Map<String, Int> = point.factors.toMap()

        val name = if(add >= 0){
            point.name + " and index[$key] + " + add.toString()
        }else{
            point.name + " and index[$key] " + add.toString()
        }

        val newPoint = point.copy(name = name, factors = f as HashMap<String, Int>)
        newPoint.factors[key] = point.factors[key]!! + add
        dp.offPoints[name] = newPoint
    }

    private fun removeSameParameter(ic: ArrayList<String>, parsedCondition: HashMap<String, String>): Int? {
        for((i, _ic) in ic.withIndex()){
            val _parsedCondition = ExpectedOutputDataGenerator.makeParsedCondition(_ic)
            if(parsedCondition["left"] == _parsedCondition["left"]
                    && parsedCondition["right"] != _parsedCondition["right"]){
                return i
            }
        }
        return null
    }

    private fun generateOnPoints(dp: DomainPoints, condition: ConditionAndReturnValueList.ConditionAndReturnValue) {
            val ifCondition = condition.conditions

            for (i in 0 until ifCondition.size) {
                val b: ArrayList<Boolean> = ArrayList(condition.bools)

                var ic = ArrayList<String>(ifCondition)
                val parsedCondition = ExpectedOutputDataGenerator.makeParsedCondition(ic[i])
                ic[i] = parsedCondition["left"] + "=" + parsedCondition["right"]

                var op = 0
                if(b[i]) {
                    op = when(parsedCondition["operator"]){
                        ">"-> 1
                        "<"-> -1
                        else -> 0
                    }
                }else{
                    op = when(parsedCondition["operator"]){
                        ">="-> -1
                        "<="-> 1
                        else -> 0
                    }
                    b[i] = true
                }
                val localIc = ic[i]
                val index = removeSameParameter(ic, parsedCondition)
                if (index != null) {
                    val copyic: ArrayList<String> = ArrayList(ic)
                    copyic.removeAt(index)
                    ic = ArrayList(copyic)
                }
                val result = generatePoint(ic, b, localIc, parsedCondition["left"].toString(), 2, eqAlith=op)
                if (result != null) {
                    dp.onPoints["${condition.returnStr} $localIc $b"] = result
                }
            }
    }

    private fun generateOutPoints(dp: DomainPoints, condition: ConditionAndReturnValueList.ConditionAndReturnValue) {
            val ifCondition = condition.conditions
            val bools: ArrayList<ArrayList<Boolean>> = ArrayList()
            for (i in 0 until ifCondition.size) {
                val b = ArrayList(condition.bools)
                bools.add(b)
            }
            for ((i, b) in bools.withIndex()) {
                b[i] = false
                val name = ifCondition[i]
                val type = ExpectedOutputDataGenerator.makeParsedCondition(name)["left"].toString()

                val result = generatePoint(ifCondition, b, name, type, 2)
                if (result != null) {
                    dp.outPoints["${condition.returnStr} $name $b"] = result
                }
            }
    }

    private fun generateInPoints(dp: DomainPoints, condition: ConditionAndReturnValueList.ConditionAndReturnValue) {
            val ifCondition = condition.conditions
            val b: ArrayList<Boolean> = condition.bools

            val name = ifCondition.joinToString(separator = " and ")
            val type = "all"
            val result = generatePoint(ifCondition, b, name, type, 2)
            if (result != null) {
                dp.inPoints["$name $b"] = result
            }
    }


    private fun generatePoint(ifCondition: ArrayList<String>, bools: ArrayList<Boolean>,
                              name: String, type: String, buf: Int=0,
                              eqAlith: Int = 0): Point? {
        var conditionUnion = ctx.mkBool(java.lang.Boolean.TRUE) //単位元としてTRUEの式を一つくっつけとく
        var expr: BoolExpr?
        for (i in ifCondition.indices) {
            val parsedCondition = ExpectedOutputDataGenerator.makeParsedCondition(ifCondition[i])
            val bool = bools[i]
            val operator = parsedCondition["operator"]
            val alith = when(bool){
                true -> buf
                false -> -buf
            }
            expr = if (Util.getOperator(parsedCondition["left"]!!) == "+") {
                makePlusExpr(
                        parsedCondition["right"]!!,
                        operator!!,
                        parsedCondition["left"]!!,
                        bool,
                        alith,
                        eqAlith)
            } else {
                makeInequalityExpr(
                        parsedCondition["right"]!!,
                        operator!!,
                        parsedCondition["left"]!!,
                        bool,
                        alith,
                        eqAlith)

            }

            if (!bool){
                assert(expr != null)
                expr = ctx.mkNot(expr!!)
            }

            conditionUnion = ctx.mkAnd(conditionUnion, expr)
        }
        expr = null

        for (i in functionDefinition.parameters.indices) {
            if (functionDefinition.argumentTypes[i] != "int") { //int型なら0以上の制限はいらない
                val _expr =  makeInequalityExpr("0", ">", functionDefinition.parameters[i], true)
                if(expr == null){
                    expr = _expr
                }else {
                    expr = ctx.mkAnd(expr, _expr)
                }
            }
        }
        if (expr != null) {
            conditionUnion = ctx.mkAnd(conditionUnion, expr)
        }

        val solver = ctx.mkSolver()
        solver.add(conditionUnion)
        val point = Point(name, type, bools)
        if (solver.check() == Status.SATISFIABLE) {
            val m = solver.model
            functionDefinition.parameters.forEach { p ->
                val v = m.evaluate(ctx.mkIntConst(p), false)
                point.factors[p] = v.toString().toInt()
            }
            return point
        }
        return null
    }

    private fun makePlusExpr(_right: String, operator: String, _left: String, bool: Boolean, alith: Int=0, eqAlith: Int=0): BoolExpr? {
        val str = _left.split("+")
        val left = ctx.mkIntConst(str[0])
        val right = ctx.mkIntConst(str[1])
        val arithes: ArrayList<ArithExpr> = ArrayList()
        arithes.add(left)
        arithes.add(right)
        val plus = ctx.mkAdd(left, right)
        return when (operator) {
            "<" -> ctx.mkLt(plus, ctx.mkInt(_right.toInt() - alith))
            "<=" -> ctx.mkLe(plus, ctx.mkInt(_right.toInt() - alith))
            ">" -> ctx.mkGt(plus, ctx.mkInt(_right.toInt() + alith))
            ">=" -> ctx.mkGe(plus, ctx.mkInt(_right.toInt() + alith))
            "=" -> ctx.mkEq(plus, ctx.mkInt(_right.toInt() + eqAlith))
            else -> null
        }
    }

    private fun makeInequalityExpr(_right: String, operator: String, _left: String, bool: Boolean, alith: Int=0, eqAlith: Int=0): BoolExpr? {
        val right = java.lang.Long.valueOf(_right)
        return when (operator) {
            "<" -> ctx.mkLt(ctx.mkIntConst(_left), ctx.mkInt(right - alith))
            "<=" -> ctx.mkLe(ctx.mkIntConst(_left), ctx.mkInt(right - alith))
            ">" -> ctx.mkGt(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            ">=" -> ctx.mkGe(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            "=" -> ctx.mkEq(ctx.mkIntConst(_left), ctx.mkInt(right + eqAlith))
            else -> null
        }
    }
}

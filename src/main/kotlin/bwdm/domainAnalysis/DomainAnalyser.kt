package bwdm.domainAnalysis

import bwdm.Util
import bwdm.boundaryValueAnalysisUnit.ExpectedOutputDataGenerator
import bwdm.informationStore.InformationExtractor
import com.microsoft.z3.ArithExpr
import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Status

class DomainAnalyser(private val ie: InformationExtractor){
    val onPoints:HashMap<String, Point> = HashMap()
    val offPoints:HashMap<String, Point> = HashMap()
    val inPoints:HashMap<String, Point> = HashMap()
    val outPoints:HashMap<String, Point> = HashMap()
    private val ctx: Context = Context()

    init {
        generateOutPoints()
        generateInPoints()
        generateOnPoints()
        generateOffPoints()
    }

    val allTestcasesByDa: String
        get() {
            val buf = StringBuilder()
            addResultBuf(buf, onPoints, "Onポイント")
            addResultBuf(buf, offPoints, "Offポイント")
            addResultBuf(buf, inPoints, "Inポイント")
            addResultBuf(buf, outPoints, "Outポイント")

            return buf.toString()
        }

    private fun addResultBuf(buf: StringBuilder, points: HashMap<String, Point>, title: String){
        var i: Int = 0
        buf.append("- $title\n")
        for ((k, p) in points) {
            buf.append("No.").append(i + 1).append(" : ")
            for (prm in ie.parameters) {
                buf.append(p.factors[prm]).append(" ")
            }
            buf.append("-> ").append("not yet").append("\n")

            i++
        }
    }

    private fun generateOffPoints() {
        for ((k, p) in onPoints) {
            if(Util.getOperator(p.type) == "+"){
                for((k_, f) in p.factors) {
                    copyOffPoint(p, k_, 1)
                    copyOffPoint(p, k_, -1)
                }
            }else{
                copyOffPoint(p, p.type, 1)
                copyOffPoint(p, p.type, -1)
            }
        }
    }

    private fun copyOffPoint(point: Point, key: String, add: Int = 0){
        val f: Map<String, Int> = point.factors.toMap()

        val name = if(add >= 0){
            point.name + " and index[$key] + " + add.toString()
        }else{
            point.name + " and index[$key] " + add.toString()
        }

        val newPoint = point.copy(name = name, factors = f as HashMap<String, Int>)
        newPoint.factors[key] = point.factors[key]!! + add
        offPoints[name] = newPoint
    }

    private fun generateOnPoints() {
        val ifCondition = ie.ifConditionBodiesInCameForward
        val b = arrayListOf(true, true, true)

        for (i in 0..2) {
            val ic = ArrayList<String>(ifCondition)
            val parsedCondition = ExpectedOutputDataGenerator.makeParsedCondition(ic[i])
            ic[i] = parsedCondition["left"] + "=" + parsedCondition["right"]
            onPoints[ic[i]] = generatePoint(ic, b, ic[i], parsedCondition["left"].toString(), 2)
        }
    }

    private fun generateOutPoints() {
        val ifCondition = ie.ifConditionBodiesInCameForward
        val bools: ArrayList<ArrayList<Boolean>> = ArrayList()
        bools.add(arrayListOf(false, true, true))
        bools.add(arrayListOf(true, false, true))
        bools.add(arrayListOf(true, true, false))

        for ((i, b) in bools.withIndex()) {
            val name = ifCondition[i]
            val type = ExpectedOutputDataGenerator.makeParsedCondition(name)["left"].toString()
            outPoints[name] = generatePoint(ifCondition, b, name, type,  2)
        }
    }

    private fun generateInPoints() {
        val ifCondition = ie.ifConditionBodiesInCameForward
        val bools: ArrayList<ArrayList<Boolean>> = ArrayList()
        bools.add(arrayListOf(true, true, true))

        val name = ifCondition.joinToString(separator = " and ")
        val type = "all"
        for (b in bools) {
            inPoints[name] = generatePoint(ifCondition, b, name, type, 2)
        }
    }


    private fun generatePoint(ifCondition: ArrayList<String>, bools: ArrayList<Boolean>,
                              name: String, type: String, buf: Int=0): Point {
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
                        alith)
            } else {
                makeInequalityExpr(
                        parsedCondition["right"]!!,
                        operator!!,
                        parsedCondition["left"]!!,
                        bool,
                        alith)

            }

            if (!bool){
                assert(expr != null)
                expr = ctx.mkNot(expr!!)
            }

            conditionUnion = ctx.mkAnd(conditionUnion, expr)
        }
        expr = null

        for (i in ie.parameters.indices) {
            if (ie.argumentTypes[i] != "int") { //int型なら0以上の制限はいらない
                expr = makeInequalityExpr(ie.parameters[i], ">", "0", true)
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
            ie.parameters.forEach { p ->
                val v = m.evaluate(ctx.mkIntConst(p), false)
                point.factors[p] = v.toString().toInt()
            }
        }
        return point
    }

    private fun makePlusExpr(_right: String, operator: String, _left: String, bool: Boolean, alith: Int=0): BoolExpr? {
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
            "=" -> ctx.mkEq(plus, ctx.mkInt(_right.toInt()))
            else -> null
        }
    }

    private fun makeInequalityExpr(_right: String, operator: String, _left: String, bool: Boolean, alith: Int=0): BoolExpr? {
        val right = java.lang.Long.valueOf(_right)
        return when (operator) {
            "<" -> ctx.mkLt(ctx.mkIntConst(_left), ctx.mkInt(right - alith))
            "<=" -> ctx.mkLe(ctx.mkIntConst(_left), ctx.mkInt(right - alith))
            ">" -> ctx.mkGt(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            ">=" -> ctx.mkGe(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            "=" -> ctx.mkEq(ctx.mkIntConst(_left), ctx.mkInt(right))
            else -> null
        }
    }
}
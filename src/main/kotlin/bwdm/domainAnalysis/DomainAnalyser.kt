package bwdm.domainAnalysis

import bwdm.Util
import bwdm.boundaryValueAnalysisUnit.ExpectedOutputDataGenerator
import bwdm.informationStore.InformationExtractor
import com.microsoft.z3.ArithExpr
import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Status
import java.util.ArrayList

class DomainAnalyser(private val ie: InformationExtractor){
//    val OnPoints: ArrayList<Point> = ArrayList()
  //  val OffPoints:ArrayList<Point> = ArrayList()
    //val InPoints:ArrayList<Point> = ArrayList()
    val outPoints:ArrayList<Point> = ArrayList()
    private var factors: HashMap<String, Factor> = HashMap()
    private val ctx: Context = Context()

    init {
        makeFactors()
        generateOutPoints()
    }

    private fun makeFactors(){
        ie.parameters.forEach{parameter ->
            factors[parameter] = Factor(parameter)
        }
    }


    private fun generateOutPoints() {
        val ifCondition = ie.ifConditionBodiesInCameForward
        val bools: ArrayList<ArrayList<Boolean>> = ArrayList()
        bools.add(arrayListOf(false, true, true))
        bools.add(arrayListOf(true, false, true))
        bools.add(arrayListOf(true, true, false))

        for (b in bools) {
            outPoints.add(generateOutPoint(ifCondition, b))
        }
    }


    private fun generateOutPoint(ifCondition: ArrayList<String>, bools: ArrayList<Boolean>): Point {
        var conditionUnion = ctx.mkBool(java.lang.Boolean.TRUE) //単位元としてTRUEの式を一つくっつけとく
        var expr: BoolExpr?
        for (i in ifCondition.indices) {
            val parsedCondition = ExpectedOutputDataGenerator.makeParsedCondition(ifCondition[i])
            val bool = bools[i]
            val operator = parsedCondition["operator"]
            val alith = when(bool){
                true -> 2
                false -> -2
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
        val point = Point()
        if (solver.check() == Status.SATISFIABLE) {
            val m = solver.model
            ie.parameters.forEach { p ->
                val v = m.evaluate(ctx.mkIntConst(p), false)
                val f = Factor(p, v.toString().toInt())
                point.factors.add(f)
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
            "<" -> ctx.mkLt(plus, ctx.mkInt(_right.toInt() + alith))
            "<=" -> ctx.mkLe(plus, ctx.mkInt(_right.toInt() + alith))
            ">" -> ctx.mkGt(plus, ctx.mkInt(_right.toInt() + alith))
            ">=" -> ctx.mkGe(plus, ctx.mkInt(_right.toInt() + alith))
            else -> null
        }
    }

    private fun makeInequalityExpr(_right: String, operator: String, _left: String, bool: Boolean, alith: Int=0): BoolExpr? {
        val right = java.lang.Long.valueOf(_right)
        return when (operator) {
            "<" -> ctx.mkLt(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            "<=" -> ctx.mkLe(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            ">" -> ctx.mkGt(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            ">=" -> ctx.mkGe(ctx.mkIntConst(_left), ctx.mkInt(right + alith))
            else -> null
        }
    }
}
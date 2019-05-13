package bwdm.domainAnalysis

import bwdm.Util
import bwdm.informationStore.InformationExtractor
import bwdm.symbolicExecutionUnit.SeUnitMain
import com.microsoft.z3.ArithExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Status
import java.util.ArrayList

class DomainAnalyser(private val ie: InformationExtractor){
//    val OnPoints: ArrayList<Point> = ArrayList()
  //  val OffPoints:ArrayList<Point> = ArrayList()
    //val InPoints:ArrayList<Point> = ArrayList()
    val OutPoints:ArrayList<Point> = ArrayList()
    private var factors: HashMap<String, Factor> = HashMap()
    private val ctx: Context = Context()

    init {
        makeFactors()
    }

    private fun makeFactors(){
        ie.parameters.forEach{parameter ->
            factors[parameter] = Factor(parameter)
        }
    }

    private fun generateOnPointValue(c: HashMap<String, String>): Int? {
        val params = c["left"]!!.split("+")
        val left = ctx.mkIntConst(params[0])
        val right = ctx.mkIntConst(params[1])
        val arithes: ArrayList<ArithExpr> = ArrayList()
        arithes.add(left)
        arithes.add(right)
        val plus = ctx.mkAdd(left, right)
        val expr = ctx.mkEq(plus, ctx.mkInt(c["right"]))
        val solver = ctx.mkSolver()
        solver.add(expr)

        if (solver.check() != Status.SATISFIABLE) {
            return null
        }
        val m = solver.model
        val leftValue = m.evaluate(left, false).toString().toInt()
        val rightValue = m.evaluate(right, false).toString().toInt()

        return leftValue
    }

    private fun makeOnPoints(){
        ie.ifConditions.forEach {condition ->
            val c = condition.value[0]
            if(!factors.containsKey(c["left"])){
                generateOnPointValue(c)
            }
            val factor = factors[c["left"]]!!.copy()
            factor.value = c["right"]!!.toInt()
            //val point = Point()
        }
    }
}
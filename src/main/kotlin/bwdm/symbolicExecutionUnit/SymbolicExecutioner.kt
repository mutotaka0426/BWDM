package bwdm.symbolicExecutionUnit

import bwdm.informationStore.InformationExtractor
import bwdm.informationStore.ConditionAndReturnValueList.ConditionAndReturnValue
import bwdm.Util
import com.microsoft.z3.*

import bwdm.boundaryValueAnalysisUnit.ExpectedOutputDataGenerator.Companion.makeParsedCondition

import java.util.ArrayList
import java.util.HashMap
import java.util.function.Consumer

class SymbolicExecutioner//各条件式は左辺右辺のうち片方のみが変数であるという制約付き
internal constructor(private val ie: InformationExtractor) {

    //複数の変数があっても、条件式次第で一つの変数の値次第で、
    //残りの変数の値はどうでもいい場合がある
    //その場合、z3はevaluateした場合、変数名をそのまま返すので、
    //<String, String>としてある
    val inputDataList: ArrayList<HashMap<String, String>> = ArrayList()

    private val expectedOutputDataList: ArrayList<String> = ArrayList()
    private val ctx: Context = Context()

    init {
        val conditionAndReturnValueList = ie.conditionAndReturnValueList

        conditionAndReturnValueList.conditionAndReturnValues.forEach(Consumer<ConditionAndReturnValue> { this.doSymbolicExecution(it) })
    }

    private fun doSymbolicExecution(_conditionAndReturnValue: ConditionAndReturnValue) {
        val conditions = _conditionAndReturnValue.conditions
        val bools = _conditionAndReturnValue.bools

        var conditionUnion = ctx.mkBool(java.lang.Boolean.TRUE) //単位元としてTRUEの式を一つくっつけとく
        var expr: BoolExpr?
        for (i in conditions.indices) {
            val parsedCondition = makeParsedCondition(conditions[i])
            val bool = bools[i]
            val operator = parsedCondition["operator"]

            expr = if (operator == "mod") { //剰余式
                makeModExpr(
                        parsedCondition["left"]!!,
                        parsedCondition["right"]!!,
                        parsedCondition["surplus"]!!,
                        bool
                )
            } else { //不等式
                makeInequalityExpr(
                        parsedCondition["left"]!!,
                        operator!!,
                        parsedCondition["right"]!!,
                        bool
                )
            }

            conditionUnion = ctx.mkAnd(conditionUnion, expr)
        }
        expr = null
        var parameters = ie.parameters
        val argumentTypes = ie.argumentTypes

        for (i in parameters.indices) {
            if (argumentTypes[i] != "int") { //int型なら0以上の制限はいらない
                expr = makeInequalityExpr(parameters[i], ">", "0", true)
            }
        }
        if (expr != null) {
            conditionUnion = ctx.mkAnd(conditionUnion, expr)
        }

        val solver = ctx.mkSolver()
        solver.add(conditionUnion)
        if (solver.check() == Status.SATISFIABLE) {
            val m = solver.model

            parameters = ie.parameters
            val hm = HashMap<String, String>()
            parameters.forEach { p -> hm[p] = m.evaluate(ctx.mkIntConst(p), false).toString() }

            inputDataList.add(hm)
            expectedOutputDataList.add(_conditionAndReturnValue.returnStr!!)
        }

    }


    private fun makeModExpr(_left: String, _right: String, _surplus: String, _bool: Boolean?): BoolExpr {
        val expr: BoolExpr
        val surplus = java.lang.Long.valueOf(_surplus)

        val modExpr: IntExpr
        modExpr = if (Util.isNumber(_left)) { //右辺が変数
            val left = java.lang.Long.valueOf(_left)
            ctx.mkMod(ctx.mkInt(left), ctx.mkIntConst(_right))
        } else { //左辺が変数
            val right = java.lang.Long.valueOf(_right)
            ctx.mkMod(ctx.mkIntConst(_left), ctx.mkInt(right))
        }
        expr = ctx.mkEq(modExpr, ctx.mkInt(surplus))

        return if (_bool!!)
            expr //満たすべき真偽(_bool)次第で、notをつける
        else
            ctx.mkNot(expr)
    }


    private fun makeInequalityExpr(_left: String, _operator: String, _right: String, _bool: Boolean?): BoolExpr? {
        val expr: BoolExpr?

        if (Util.isNumber(_left)) { //右辺が変数
            val left = java.lang.Long.valueOf(_left)

            expr = when (_operator) {
                "<" -> ctx.mkLt(ctx.mkInt(left), ctx.mkIntConst(_right))
                "<=" -> ctx.mkLe(ctx.mkInt(left), ctx.mkIntConst(_right))
                ">" -> ctx.mkGt(ctx.mkInt(left), ctx.mkIntConst(_right))
                ">=" -> ctx.mkGe(ctx.mkInt(left), ctx.mkIntConst(_right))
                else -> null
            }

        } else { //左辺が変数
            val right = java.lang.Long.valueOf(_right)
            expr = when (_operator) {
                "<" -> ctx.mkLt(ctx.mkIntConst(_left), ctx.mkInt(right))
                "<=" -> ctx.mkLe(ctx.mkIntConst(_left), ctx.mkInt(right))
                ">" -> ctx.mkGt(ctx.mkIntConst(_left), ctx.mkInt(right))
                ">=" -> ctx.mkGe(ctx.mkIntConst(_left), ctx.mkInt(right))
                else -> null
            }
        }

        return if (_bool!!)
            expr //満たすべき真偽(_bool)次第で、notをつける
        else {
            assert(expr != null)
            ctx.mkNot(expr!!)
        }
    }

    fun getExpectedOutputDataList(): ArrayList<String> {
        return expectedOutputDataList
    }

}
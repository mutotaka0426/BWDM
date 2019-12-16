package com.github.korosuke613.bwdm.symbolicExecutionUnit

import com.github.korosuke613.bwdm.Analyzer
import com.github.korosuke613.bwdm.Util
import com.github.korosuke613.bwdm.boundaryValueAnalysisUnit.ExpectedOutputDataGenerator.Companion.makeParsedCondition
import com.github.korosuke613.bwdm.informationStore.ConditionAndReturnValueList.ConditionAndReturnValue
import com.github.korosuke613.bwdm.informationStore.FunctionDefinition
import com.microsoft.z3.*
import java.util.*
import java.util.function.Consumer

class SymbolicExecutioner
(functionDefinition: FunctionDefinition) : Analyzer<String>(functionDefinition) {
    //各条件式は左辺右辺のうち片方のみが変数であるという制約付き

    //複数の変数があっても、条件式次第で一つの変数の値次第で、
    //残りの変数の値はどうでもいい場合がある
    //その場合、z3はevaluateした場合、変数名をそのまま返すので、
    //<String, String>としてある

    private val expectedOutputDataList: ArrayList<String> = ArrayList()
    private val ctx: Context = Context()

    init {
        val conditionAndReturnValueList = functionDefinition.conditionAndReturnValueList

        conditionAndReturnValueList!!.conditionAndReturnValues.forEach(Consumer<ConditionAndReturnValue> {
            this.doSymbolicExecution(it)
        })
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
                if (Util.getOperator(parsedCondition["left"]!!) == "+") {
                    val str = parsedCondition["left"]!!.split("+")
                    val left = ctx.mkIntConst(str[0])
                    val right = ctx.mkIntConst(str[1])
                    val arithes: ArrayList<ArithExpr> = ArrayList()
                    arithes.add(left)
                    arithes.add(right)
                    val plus = ctx.mkAdd(left, right)
                    expr = when (operator) {
                        "<" -> ctx.mkLt(plus, ctx.mkInt(parsedCondition["right"]))
                        "<=" -> ctx.mkLe(plus, ctx.mkInt(parsedCondition["right"]))
                        ">" -> ctx.mkGt(plus, ctx.mkInt(parsedCondition["right"]))
                        ">=" -> ctx.mkGe(plus, ctx.mkInt(parsedCondition["right"]))
                        else -> null
                    }
                    if (bool)
                        expr //満たすべき真偽(_bool)次第で、notをつける
                    else {
                        assert(expr != null)
                        ctx.mkNot(expr!!)
                    }
                } else {
                    makeInequalityExpr(
                            parsedCondition["left"]!!,
                            operator!!,
                            parsedCondition["right"]!!,
                            bool
                    )
                }
            }

            conditionUnion = ctx.mkAnd(conditionUnion, expr)
        }
        expr = null
        var parameters = definition.parameters
        val argumentTypes = definition.argumentTypes

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

            parameters = definition.parameters
            val inputData = HashMap<String, String>()
            parameters.forEach { p -> inputData[p] = m.evaluate(ctx.mkIntConst(p), false).toString() }

            inputDataList.add(inputData)
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

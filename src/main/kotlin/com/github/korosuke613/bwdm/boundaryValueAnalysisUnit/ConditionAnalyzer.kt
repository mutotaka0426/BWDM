package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import java.lang.reflect.Method
import com.github.korosuke613.bwdm.Util

class ConditionAnalyzer {
    companion object {

		// 受け取った式の真偽を評価する(expressionは数値と演算子のみ)
        fun evaluteExpression(expression: String): Boolean {
			// 条件式がない場合は真
			if (expression == "") return true
			
			val exp = summarizeExpression(expression)
			val expList = exp.split(" ").toMutableList()
			
			val operator = Util.getOperator(exp)
			val indexOfoperator = expList.indexOf(operator)
			try {
				val left = expList[indexOfoperator - 1].toLong()
				val right = expList[indexOfoperator + 1].toLong()

				when(operator) {
					">" -> {
						if(left > right) {
							return true
						} else {
							return false
						}
					}
					"<" -> {
						if(left < right) {
							return true
						} else {
							return false
						}
					}
					">=" -> {
						if(left >= right) {
							return true
						} else {
							return false
						}
					}
					"<=" -> {
						if(left <= right) {
							return true
						} else {
							return false
						}
					}
					"=" -> {
						if(left == right) {
							return true
						} else {
							return false
						}
					}
					"<>" -> {
						if(left != right) {
							return true
						} else {
							return false
						}
					}
				}
			} catch (e : NumberFormatException) {
				return true
			}
            return true 
        }

		fun summarizeExpression(expression: String): String {
			var exp = "(" + expression + ")"
			
			while (exp.contains("(")) {
				var indexOfleft = exp.lastIndexOf("(")
				var indexOfright = exp.indexOf(")", indexOfleft) + 1
				var leftExp = exp.take(indexOfleft)
				var rightExp = exp.substring(indexOfright)

				exp = omitOperator(exp.substring(indexOfleft, indexOfright), "*")
				exp = omitOperator(exp, "/")
				exp = omitOperator(exp, "mod")
				exp = omitOperator(exp, "+")
				exp = omitOperator(exp, "-")

				exp = leftExp + exp + rightExp
			} 
			

			return exp
		}

		fun omitOperator(expression: String, operator: String): String {
			var exp = expression.split(" ", "(", ")").toMutableList() 
			exp.removeAll { it == "" }
			while (exp.contains(operator)) {
				var index = exp.indexOf(operator)
				var left = index - 1
				var right = index + 1
				try {
					var leftVl = exp[left].toLong()
					var rightVl = exp[right].toLong()

					when(operator) {
						"+" -> {
							exp[index] = (leftVl + rightVl).toString()
						}
						"-" -> {
							exp[index] = (leftVl - rightVl).toString()
						}
						"*" -> {
							exp[index] = (leftVl * rightVl).toString()
						}
						"/" -> {
							exp[index] = (leftVl / rightVl).toString()
						}
						"mod" -> {
							exp[index] = (leftVl % rightVl).toString()
						}
					}
					exp.removeAt(right)
					exp.removeAt(left)
				} catch (e : NumberFormatException) {
					break
				}
			}
			val returnExp = StringBuilder()
			exp.forEach {
				returnExp.append(it)
				returnExp.append(" ")
			}
			return String(returnExp) 
		}

	}
}

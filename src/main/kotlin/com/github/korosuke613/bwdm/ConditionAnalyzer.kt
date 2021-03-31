package com.github.korosuke613.bwdm

class ConditionAnalyzer {
    companion object {
		internal const val TRUE: String = "TRUE"
		internal const val FALSE: String = "FALSE"
		internal var testNumber: Int = 1

		// 受け取った式の真偽を評価する(expressionは数値と演算子のみ)
        fun evaluateExpression(expression: String): Boolean {
			// 条件式がない場合は真
			if (expression == "") return true

			val expTree = expressionTree(expression)
			val logTree = logicTree(expTree)

			// ↓これで流れが見えるよ♡
			//print("No." + testNumber++ + " : " )
			//print(expression)
			//print(" ==> ")
			//print(expTree)
			//print(" ==> ")
			//println(logTree)
			
			if(logTree == TRUE) return true
			else if (logTree == FALSE) return false
			// 式を評価しきれなかった場合，とりあえずtrue
            else {
				println("\"$expression\" did not evaluate")
				return true 
			}
        }

		fun expressionTree(expression_: String): String {
			if(expression_ == "") return ""

			val expression = expression_.removePrefix("(").removeSuffix(")")

			if(expression.split(" ").size == 1) {
				return expression
			}

			val indexOfLeftTail = getOperand(expression)
			val indexOfOperatorTail = getOperand(expression, indexOfLeftTail + 2)
			
			val leftOperand = expression.substring(0, indexOfLeftTail)
			val operator = expression.substring(indexOfLeftTail + 1, indexOfOperatorTail)
			val rightOperand = expression.substring(indexOfOperatorTail + 1)
			
			return calculateOperator(expressionTree(leftOperand), expressionTree(rightOperand), operator)
		}

		// オペランドの末尾のインデックスを返す
		fun getOperand(expression: String, index: Int = 0): Int {
			val expCharList = expression.toCharArray()

			if(expCharList[index] != '('){
				for(i in expCharList.indices){
					if(expCharList[index + i] == ' ') return index + i
				}
				return -1
			} else {
				var leftCount: Int = 0
				var rightCount: Int = 0

				for(i in expCharList.indices){
					if(expCharList[index + i] == '(') leftCount++
					else if (expCharList[index + i] == ')') rightCount++

					if(leftCount == rightCount) return i + 1
				}
				return -1
			}
		}

		fun calculateOperator(leftOperand: String, rightOperand: String, operator: String): String {
			try {
				var leftValue = leftOperand.toDouble()
				var rightValue = rightOperand.toDouble()

				when(operator) {
					"+" -> {
						return (leftValue + rightValue).toString()
					}
					"-" -> {
						return (leftValue - rightValue).toString()
					}
					"*" -> {
						return (leftValue * rightValue).toString()
					}
					"/" -> {
						return (leftValue / rightValue).toString()
					}
					"mod" -> {
						return (leftValue % rightValue).toString()
					}
					"<" -> {
						return if(leftValue < rightValue) TRUE else FALSE
					}
					"<=" -> {
						return if(leftValue <= rightValue) TRUE else FALSE
					}
					">" -> {
						return if(leftValue > rightValue) TRUE else FALSE
					}
					">=" -> {
						return if(leftValue >= rightValue) TRUE else FALSE
					}
					"=" -> {
						return if(leftValue == rightValue) TRUE else FALSE
					}
					"<>" -> {
						return if(leftValue != rightValue) TRUE else FALSE
					}
				}
			} catch (e : NumberFormatException) {
				// 何もしない
			}
			// 計算できない場合，括弧を付け直して返す
		//	print("計算できないぞ　→　")
		//	println("($leftOperand $operator $rightOperand)")
			return "($leftOperand $operator $rightOperand)"
		}

		fun logicTree(expression_: String): String {
			if(expression_ == "") return ""

			val expression = expression_.removePrefix("(").removeSuffix(")")

			if(expression.split(" ").size == 1) {
				return expression
			}

			val indexOfLeftTail = getOperand(expression)
			val indexOfOperatorTail = getOperand(expression, indexOfLeftTail + 2)
			
			val leftBoolean = expression.substring(0, indexOfLeftTail)
			val operator = expression.substring(indexOfLeftTail + 1, indexOfOperatorTail)
			val rightBoolean = expression.substring(indexOfOperatorTail + 1)
			
			return solveLogic(logicTree(leftBoolean), logicTree(rightBoolean), operator)
		}

		fun solveLogic(leftBoolean: String, rightBoolean: String, operator: String): String {
			when(operator) {
				"or" -> {
					return if((leftBoolean == TRUE) || (rightBoolean == TRUE)) TRUE else FALSE
				}
				"and" -> {
					return if((leftBoolean == TRUE) && (rightBoolean == TRUE)) TRUE else FALSE
				}
				"=" -> {
					return if((leftBoolean == rightBoolean) && ((leftBoolean == TRUE) || (leftBoolean == FALSE))) TRUE else FALSE
				}
				"<>" -> {
					return if(((leftBoolean == FALSE) && (rightBoolean == TRUE)) || ((leftBoolean == TRUE) && (rightBoolean == FALSE))) TRUE else FALSE
				}
			}
			return "($leftBoolean $operator $rightBoolean)"
		}
	}
}

// 変数を置き換える(完全一致した単語のみ置き換える)
// 例えば式中の変数aをAに置換する際に，(data = a) → (dAtA = A)となることを防ぐための拡張関数
fun String.replaceVariable(oldValue: String, newValue: String): String {
	var returnStr: String = ""
	val startRegex = Regex("""\(*${oldValue}\)*""")

	this.split(" ").forEach {
		if(startRegex.matches(it)) returnStr += it.replace(oldValue, newValue)
		else returnStr += it

		returnStr += " "
	}
	returnStr = returnStr.removeSuffix(" ")

	return returnStr
}

fun String.searchVariable(str: String): Boolean {
	val startRegex = Regex("""\(*${str}\)*""")
	this.split(" ").forEach {
		if(startRegex.matches(it)) return true
	}
	return false
}

fun String.alignParentheses(): String {
	val leftCount = this.split("(").size - 1
	val rightCount = this.split(")").size - 1

	if(leftCount > rightCount) {
		val returnLength = this.length - (leftCount - rightCount)
		return this.takeLast(returnLength)
	} else if(leftCount < rightCount) {
		val returnLength = this.length - (rightCount - leftCount)
		return this.take(returnLength)
	} else {
		return this
	}
}

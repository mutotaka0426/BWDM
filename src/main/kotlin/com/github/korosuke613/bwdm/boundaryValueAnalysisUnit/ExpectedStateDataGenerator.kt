package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import com.github.korosuke613.bwdm.informationStore.OperationDefinition
import com.github.korosuke613.bwdm.ConditionAnalyzer
import com.github.korosuke613.bwdm.replaceVariable
import java.util.*

class ExpectedStateDataGenerator
constructor(private val definition: OperationDefinition,
            _inputDataList: ArrayList<HashMap<String, Long>>) {
    internal val expectedStateDataList: ArrayList<String> = ArrayList()
	private val postInstance: ArrayList<HashMap<String, String>> = ArrayList()

    init {

		// 操作後のインスタンス変数の値を求める
		_inputDataList.forEach { inputData ->
			calculatorPostInstance(inputData)
		}

		// 期待出力を決定する
		makeExpectedOutputData(_inputDataList)

    }

	private fun calculatorPostInstance(_inputData: HashMap<String, Long>) {
		val instanceList: LinkedHashMap<String, String> = linkedMapOf()
		definition.operationExpressions.forEach {
			var expression = it.value

			// 式中の定数を実際の値に置換

			// 式中の仮引数を入力データに置換
			_inputData.forEach { inputData ->
				expression = expression.replaceVariable(inputData.key, inputData.value.toString())
			}

			// 式中のインスタンス変数を操作前の値に置換
			definition.instanceExpressions.forEach { instance ->
				expression = expression.replaceVariable(instance.key, instance.value)
			}

			instanceList[it.key] = ConditionAnalyzer.expressionTree(expression)
		}
		postInstance.add(instanceList)
	}

    private fun makeExpectedOutputData(_inputDataList: ArrayList<HashMap<String, Long>>) {

        for (i in _inputDataList.indices) {
			run {
            	val currentInputData = _inputDataList[i]

            	for (j in 0 until definition.parameters.size) {
            	    val currentTyp = definition.argumentTypes[j]
            	    val currentPrm = definition.parameters[j]
            	    val currentVl = currentInputData[currentPrm]?:0


            	    when (currentTyp) {
            	        "int" -> if (currentVl > BoundaryValueAnalyzer.intMax || currentVl < BoundaryValueAnalyzer.intMin) {
            	            expectedStateDataList.add(inputError)
							return@run
            	        }
            	        "nat" -> if (currentVl > BoundaryValueAnalyzer.natMax || currentVl < BoundaryValueAnalyzer.natMin) {
            	            expectedStateDataList.add(inputError)
							return@run
            	        }
            	        "nat1" -> if (currentVl > BoundaryValueAnalyzer.nat1Max || currentVl < BoundaryValueAnalyzer.nat1Min) {
            	            expectedStateDataList.add(inputError)
							return@run
            	        }
            	    }//nothing to do
				}

				// inputConditionsを評価する
				definition.inputConditions.forEach { _inputCondition ->
					var inputCondition = _inputCondition
					// 仮引数を実際の入力の値に置換する
					currentInputData.forEach {
						inputCondition = inputCondition.replaceVariable(it.key, it.value.toString())
					}
					if(!ConditionAnalyzer.evaluateExpression(inputCondition)) {
						expectedStateDataList.add(inputError)
						return@run
					}
				}

				// outputConditionsを評価する
				definition.outputConditions.forEach { _outputCondition ->
					var outputCondition = _outputCondition
					// 仮引数を実際の入力の値に置換する
					currentInputData.forEach {
						outputCondition = outputCondition.replaceVariable(it.key, it.value.toString())
					}

					// インスタンス変数を操作後の値に置換する
					postInstance[i].forEach {
						outputCondition = outputCondition.replaceVariable(it.key, it.value)
					}
					if(!ConditionAnalyzer.evaluateExpression(outputCondition)) {
						expectedStateDataList.add(objectNG)
						return@run
					}
				}

				expectedStateDataList.add(objectOK)
					
			}//end run
        }
    }

    companion object {
		private const val inputError = "Undefined Action"
		private const val objectNG = "異常"
		private const val objectOK = "正常"
    }
}

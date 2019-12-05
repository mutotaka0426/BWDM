package bwdm.boundaryValueAnalysisUnit

//import com.github.korosuke613.pict-java
import bwdm.Util
import bwdm.informationStore.InformationExtractor
import java.util.*
import java.util.stream.Collectors
import com.github.korosuke613.pict4java.Factor
import com.github.korosuke613.pict4java.Model
import com.github.korosuke613.pict4java.Pict

typealias BoundaryValueList = HashMap<String, ArrayList<Long>>
typealias InputDataList = ArrayList<HashMap<String, Long>>

class BoundaryValueAnalyzer(_information: InformationExtractor, isPairwise: Boolean = true) {

    val boundaryValueList: BoundaryValueList = HashMap()
    val inputDataList: InputDataList = ArrayList()

    init {
        //generation of instance of each parameter
        _information.parameters.forEach { p -> boundaryValueList[p] = ArrayList() }

        generateTypeBoundaryValue(_information)
        generateIfConditionalBoundaryValue(_information)

        //remove overlapped values
        val parameters = _information.parameters
        for (i in 0 until boundaryValueList.size) {
            val parameter = parameters[i]
            var bvs = boundaryValueList[parameter]!!
            bvs = bvs.stream().distinct().collect(Collectors.toList()) as ArrayList<Long>

            boundaryValueList[parameter] = bvs
        }
        if(isPairwise) {
            makeInputDataListWithPairwise(_information)
        }else{
            makeInputDataList(_information)
        }
    }


    private fun generateTypeBoundaryValue(_information: InformationExtractor) {
        val parameters = _information.parameters
        val argumentTypes = _information.argumentTypes

        for (i in argumentTypes.indices) {
            val parameter = parameters[i]
            val argumentType = argumentTypes[i]
            val bvs = boundaryValueList[parameter]
            when (argumentType) {
                "int" -> {
                    bvs!!.run {
                        add(intMax + 1)
                        add(intMax)
                        add(intMin)
                        add(intMin - 1)
                    }
                }
                "nat" -> {
                    bvs!!.run {
                        add(natMax + 1)
                        add(natMax)
                        add(natMin)
                        add(natMin - 1)
                    }
                }
                "nat1" -> {
                    bvs!!.run {
                        add(nat1Max + 1)
                        add(nat1Max)
                        add(nat1Min)
                        add(nat1Min - 1)
                    }
                }
                else -> {
                }
            }
        }
    }


    private fun generateIfConditionalBoundaryValue(_information: InformationExtractor) {
        val allIfConditions = _information.ifConditions

        allIfConditions.forEach { parameter, ifConditions ->
            ifConditions.forEach { condition ->
                //condition : HashMap<String, String>
                val left = condition["left"]
                if (Util.getOperator(left!!) == "+") {
                    return@forEach
                }
                val operator = condition["operator"]
                val right = condition["right"]
                val bvs = boundaryValueList[parameter]

                var trueValue: Long = 0
                var falseValue: Long = 0
                val value: Long
                if (Util.isNumber(left!!)) {
                    value = java.lang.Long.parseLong(left)
                    when (operator) {
                        "<" -> {
                            trueValue = value + 1
                            falseValue = value
                        }
                        "<=" -> {
                            trueValue = value
                            falseValue = value - 1
                        }
                        ">" -> {
                            trueValue = value - 1
                            falseValue = value
                        }
                        ">=" -> {
                            trueValue = value
                            falseValue = value + 1
                        }
                        "mod" -> {
                            trueValue = value + java.lang.Long.parseLong(condition["surplus"])
                            falseValue = value + 1
                            bvs!!.add(value - 1)
                        }
                    }

                } else if (Util.isNumber(right!!)) {
                    value = java.lang.Long.parseLong(right)
                    when (operator) {
                        "<" -> {
                            trueValue = value - 1
                            falseValue = value
                        }
                        "<=" -> {
                            trueValue = value
                            falseValue = value + 1
                        }
                        ">" -> {
                            trueValue = value + 1
                            falseValue = value
                        }
                        ">=" -> {
                            trueValue = value
                            falseValue = value - 1
                        }
                        "mod" -> {
                            trueValue = value + java.lang.Long.parseLong(condition["surplus"])
                            falseValue = value + 1
                            bvs!!.add(value - 1)
                        }
                    }
                }

                bvs!!.run {
                    add(trueValue)
                    add(falseValue)
                }

            }
        }
    }

    private fun makeInputDataListWithPairwise(_information: InformationExtractor){
        val pict = Pict()
        val model = Model()
        // 因子の取得
        val parameters = _information.parameters

        // ファクターの追加
        for (prm in parameters){
            val bvs = boundaryValueList[prm]
            val factor = Factor(named_level = bvs!!.map { it.toString() }, name = prm)
            model.addFactor(factor)
        }

        // ペアワイズ分析した結果を生成
        pict.setRootModel(model)
        val tests = pict.generate()

        for (test in tests) {
            val hash = HashMap<String, Long>()
            for((index, param) in test.withIndex()){
                hash[model.factors[index].name] = param!!.toLong()
            }
            inputDataList.add(hash)
        }
    }

    private fun makeInputDataList(_information: InformationExtractor) {
        val parameters = _information.parameters

        //最初の一つ目
        val firstPrm = parameters[0]
        val firstBvs = boundaryValueList[firstPrm]
        for (i in firstBvs!!.indices) {
            inputDataList.add(HashMap())
            val hm = inputDataList[i]
            hm[firstPrm] = firstBvs[i]
        }

        //それ以降
        parameters.forEach { p ->
            if (p != firstPrm) { //最初の要素以外に対して
                val currentBvs = boundaryValueList[p]

                //inputDataListの第一引数のみを登録した状態
                val inputDataListInitialState = ArrayList(inputDataList)

                for (i in 0 until currentBvs!!.size - 1) {
                    val inputDataListTmp = InputDataList()
                    inputDataListInitialState.forEach { inputDataOriginal ->
                        //inputDataを複製
                        val inputData = HashMap<String, Long>()
                        inputDataOriginal.forEach({ key, value -> inputData[key] = value })
                        inputDataListTmp.add(inputData)
                    }
                    inputDataList.addAll(inputDataListTmp)
                }
                for ((repeatTimesOfInsert, current_bv) in currentBvs.withIndex()) {
                    val offset = repeatTimesOfInsert * inputDataListInitialState.size
                    for (k in inputDataListInitialState.indices) {
                        val inputData = inputDataList[k + offset]
                        inputData[p] = current_bv
                    }
                }

            }
        }
    } //end makeInputDatList

    companion object {
        internal const val intMax = Integer.MAX_VALUE.toLong()
        internal const val intMin = Integer.MIN_VALUE.toLong()
        internal const val natMax = intMax * 2
        internal const val natMin: Long = 0
        internal const val nat1Max = natMax + 1
        internal const val nat1Min: Long = 1
    }

}

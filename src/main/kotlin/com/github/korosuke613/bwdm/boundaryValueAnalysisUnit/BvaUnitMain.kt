package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import com.github.korosuke613.bwdm.informationStore.FunctionDefinition
import com.github.korosuke613.bwdm.informationStore.IfElseExprSyntaxTree
import java.util.*

class BvaUnitMain(private val functionDefinition: FunctionDefinition, isPairwise: Boolean) {
    private val expectedOutputDataGenerator: ExpectedOutputDataGenerator
    val boundaryValueAnalyzer: BoundaryValueAnalyzer = BoundaryValueAnalyzer(functionDefinition, isPairwise)

    val allTestcasesByBv: String
        get() {
            val buf = StringBuilder()
            val parameters = functionDefinition.parameters
            val inputDataList = boundaryValueAnalyzer.inputDataList
            val expectedOutputDataList = expectedOutputDataGenerator.expectedOutputDataList


            for (i in expectedOutputDataList.indices) {
                buf.append("No.").append(i + 1).append(" : ")
                val inputData = inputDataList[i]
                for (prm in parameters) {
                    buf.append(inputData[prm]).append(" ")
                }
                buf.append("-> ").append(expectedOutputDataList[i]).append("\n")
            }

            return buf.toString()
        }

    init {
        expectedOutputDataGenerator = ExpectedOutputDataGenerator(
                functionDefinition,
                Objects.requireNonNull<IfElseExprSyntaxTree>(functionDefinition.ifElseExprSyntaxTree).root,
                boundaryValueAnalyzer.inputDataList
        )
    }


}

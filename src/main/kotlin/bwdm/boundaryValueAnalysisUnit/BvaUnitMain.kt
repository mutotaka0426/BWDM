package bwdm.boundaryValueAnalysisUnit

import bwdm.informationStore.IfElseExprSyntaxTree
import bwdm.informationStore.InformationExtractor

import java.util.Objects

class BvaUnitMain(private val ie: InformationExtractor) {
    val boundaryValueAnalyzer: BoundaryValueAnalyzer = BoundaryValueAnalyzer(ie)
    private val expectedOutputDataGenerator: ExpectedOutputDataGenerator

    val allTestcasesByBv: String
        get() {
            val buf = StringBuilder()
            val parameters = ie.parameters
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
                ie,
                Objects.requireNonNull<IfElseExprSyntaxTree>(ie.ifElseExprSyntaxTree).root,
                boundaryValueAnalyzer.inputDataList
        )

    }


}

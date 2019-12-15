package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import com.github.korosuke613.bwdm.UnitMain
import com.github.korosuke613.bwdm.informationStore.FunctionDefinition
import com.github.korosuke613.bwdm.informationStore.IfElseExprSyntaxTree
import java.util.*

class BvaUnitMain
(functionDefinition: FunctionDefinition, isPairwise: Boolean) : UnitMain<Long>(functionDefinition) {
    private val expectedOutputDataGenerator: ExpectedOutputDataGenerator
    val bva: BoundaryValueAnalyzer = BoundaryValueAnalyzer(functionDefinition, isPairwise)

    override val allTestCases: String
        get() {
            return getTestCases(bva.inputDataList, expectedOutputDataGenerator.expectedOutputDataList)
        }

    init {
        expectedOutputDataGenerator = ExpectedOutputDataGenerator(
                functionDefinition,
                Objects.requireNonNull<IfElseExprSyntaxTree>(functionDefinition.ifElseExprSyntaxTree).root,
                bva.inputDataList
        )
    }

}

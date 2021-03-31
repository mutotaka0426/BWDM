package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import com.github.korosuke613.bwdm.UnitMain
import com.github.korosuke613.bwdm.informationStore.OperationDefinition
import com.github.korosuke613.bwdm.informationStore.IfElseExprSyntaxTree
import java.util.*

class ObjectStateAnalyzer
(definition: OperationDefinition, isPairwise: Boolean) : UnitMain<Long>(definition) {
    private val expectedStateDataGenerator: ExpectedStateDataGenerator
    override val analyzer: BoundaryValueAnalyzer = BoundaryValueAnalyzer(definition, isPairwise)

    override val allTestCases: String
        get() {
            return getTestCases(expectedStateDataGenerator.expectedStateDataList)
        }

    init {
        expectedStateDataGenerator = ExpectedStateDataGenerator(
                definition,
                analyzer.inputDataList
        )
    }

}

package com.github.korosuke613.bwdm.symbolicExecutionUnit

import com.github.korosuke613.bwdm.UnitMain
import com.github.korosuke613.bwdm.informationStore.FunctionDefinition


class SeUnitMain
(functionDefinition: FunctionDefinition) : UnitMain<String>(functionDefinition) {
    override val analyzer: SymbolicExecutioner = SymbolicExecutioner(functionDefinition)

    override val allTestCases: String
        get() {
            return getTestCases(analyzer.getExpectedOutputDataList())
        }
}

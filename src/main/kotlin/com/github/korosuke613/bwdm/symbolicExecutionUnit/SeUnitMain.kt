package com.github.korosuke613.bwdm.symbolicExecutionUnit

import com.github.korosuke613.bwdm.UnitMain
import com.github.korosuke613.bwdm.informationStore.FunctionDefinition


class SeUnitMain
(functionDefinition: FunctionDefinition) : UnitMain<String>(functionDefinition) {
    val se: SymbolicExecutioner = SymbolicExecutioner(functionDefinition)

    override val allTestCases: String
        get() {
            return getTestCases(se.inputDataList, se.getExpectedOutputDataList())
        }
}

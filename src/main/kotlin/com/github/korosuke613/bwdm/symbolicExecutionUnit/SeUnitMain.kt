package com.github.korosuke613.bwdm.symbolicExecutionUnit

import com.github.korosuke613.bwdm.informationStore.InformationExtractor


class SeUnitMain(private val ie: InformationExtractor) {
    val se: SymbolicExecutioner = SymbolicExecutioner(ie)

    val allTestcasesBySe: String
        get() {
            val buf = StringBuilder()

            val parameters = ie.parameters
            val inputDataList = se.inputDataList
            val expectedOutputDataList = se.getExpectedOutputDataList()

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

}

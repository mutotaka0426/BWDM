package com.github.korosuke613.bwdm

import com.github.korosuke613.bwdm.informationStore.FunctionDefinition

open class Analyzer<K>(val functionDefinition: FunctionDefinition) {
    val inputDataList: ArrayList<HashMap<String, K>> = ArrayList()
    val parameters = functionDefinition.parameters
}
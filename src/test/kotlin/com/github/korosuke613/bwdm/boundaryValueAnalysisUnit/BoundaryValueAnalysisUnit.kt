package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import com.github.korosuke613.bwdm.informationStore.InformationExtractor
import org.junit.jupiter.api.Test
import java.io.IOException

internal class BoundaryValueAnalyzerTest {
    @Test
    @Throws(LexException::class, ParserException::class, IOException::class)
    fun test() {
        val information = InformationExtractor("./vdm_files/Arg2_Japanese.vdmpp")
        val bvAnalyzer = BoundaryValueAnalyzer(information, false)
    }
}
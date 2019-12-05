package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit;

import com.github.korosuke613.bwdm.informationStore.InformationExtractor;
import com.fujitsu.vdmj.lex.LexException;
import com.fujitsu.vdmj.syntax.ParserException;
import com.github.korosuke613.bwdm.informationStore.InformationExtractor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BoundaryValueAnalyzerTest {


	@Test
	void test() throws LexException, ParserException, IOException {

		InformationExtractor information = new InformationExtractor("./vdm_files/Arg2_Japanese.vdmpp");
		BoundaryValueAnalyzer bvAnalyzer = new BoundaryValueAnalyzer(information, false);




	}

}

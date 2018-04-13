package bwdm.boundaryValueAnalysisUnit;

import bwdm.informationStore.InformationExtractor;
import com.fujitsu.vdmj.lex.LexException;
import com.fujitsu.vdmj.syntax.ParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BoundaryValueAnalyzerTest {


	@Test
	void test() throws LexException, ParserException, IOException {

		InformationExtractor information = new InformationExtractor("./vdm_files/Arg2_Japanese.vdmpp");
		BoundaryValueAnalyzer bvAnalyzer = new BoundaryValueAnalyzer(information);




	}

}

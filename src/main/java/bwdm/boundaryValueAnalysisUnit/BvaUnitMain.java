package bwdm.boundaryValueAnalysisUnit;

import bwdm.informationStore.InformationExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BvaUnitMain {

	private InformationExtractor ie;
	private BoundaryValueAnalyzer boundaryValueAnalyzer;
	private ExpectedOutputDataGenerator expectedOutputDataGenerator;

	public BoundaryValueAnalyzer getBoundaryValueAnalyzer() { return boundaryValueAnalyzer; }

	public BvaUnitMain(InformationExtractor _ie) {
		this.ie = _ie;
		boundaryValueAnalyzer = new BoundaryValueAnalyzer(_ie);
		expectedOutputDataGenerator =
				new ExpectedOutputDataGenerator(
						_ie,
						Objects.requireNonNull(_ie.getIfElseExprSyntaxTree()).getRoot(),
						boundaryValueAnalyzer.getInputDataList()
				);

	}

	public String getAllTestcasesByBv() {
		StringBuilder buf = new StringBuilder();
		ArrayList<String> parameters = ie.getParameters();
		ArrayList<HashMap<String, Long>> inputDataList = boundaryValueAnalyzer.getInputDataList();
		ArrayList<String> expectedOutputDataList = expectedOutputDataGenerator.getExpectedOutputDataList();


		for(int i=0; i<expectedOutputDataList.size(); i++) {
			buf.append("No.").append(i + 1).append(" : ");
			HashMap<String, Long> inputData = inputDataList.get(i);
			for(String prm : parameters) {
				buf.append(inputData.get(prm)).append(" ");
			}
			buf.append("-> ").append(expectedOutputDataList.get(i)).append("\n");
		}

		return buf.toString();
	}



}

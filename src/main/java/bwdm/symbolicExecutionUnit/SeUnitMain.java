package bwdm.symbolicExecutionUnit;

import bwdm.informationStore.InformationExtractor;

import java.util.ArrayList;
import java.util.HashMap;

public class SeUnitMain {
	private InformationExtractor ie;
	private SymbolicExecutioner se;
	public SeUnitMain(InformationExtractor _ie) {
		this.ie = _ie;
		se = new SymbolicExecutioner(ie);
	}

	public String getAllTestcasesBySe() {
		StringBuilder buf = new StringBuilder();

		ArrayList<String> parameters = ie.getParameters();
		ArrayList<HashMap<String, String>> inputDataList = se.getInputDataList();
		ArrayList<String> expectedOutputDataList = se.getExpectedOutputDataList();

		for(int i=0; i<expectedOutputDataList.size(); i++) {
			buf.append("No.").append(i + 1).append(" : ");
			HashMap<String, String> inputData = inputDataList.get(i);
			for(String prm : parameters) {
				buf.append(inputData.get(prm)).append(" ");
			}
			 buf.append("-> ").append(expectedOutputDataList.get(i)).append("\n");
		}
		return buf.toString();
	}

	public SymbolicExecutioner getSe() { return se; }
}

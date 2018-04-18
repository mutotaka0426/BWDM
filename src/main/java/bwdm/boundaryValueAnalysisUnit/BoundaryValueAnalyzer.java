package bwdm.boundaryValueAnalysisUnit;

import bwdm.informationStore.InformationExtractor;
import bwdm.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class BoundaryValueAnalyzer {

	static final long intMax  = Integer.MAX_VALUE;
	static final long intMin  = Integer.MIN_VALUE;
	static final long natMax  = intMax * 2;
	static final long natMin  = 0;
	static final long nat1Max = natMax + 1;
	static final long nat1Min = 1;

	private HashMap<String, ArrayList<Long>> boundaryValueList;
	private ArrayList<HashMap<String, Long>> inputDataList;


	public BoundaryValueAnalyzer(InformationExtractor _information) {
		//generation of instance of each parameter
		boundaryValueList = new HashMap<>();
		inputDataList = new ArrayList<>();
		_information.getParameters().forEach(p -> boundaryValueList.put(p, new ArrayList<>()));

		generateTypeBoundaryValue(_information);
		generateIfConditionalBoundaryValue(_information);

		//remove overlapped values
		ArrayList<String> parameters = _information.getParameters();
		for(int i = 0; i < boundaryValueList.size(); i++) {
			String parameter = parameters.get(i);
			ArrayList<Long> bvs = boundaryValueList.get(parameter);
			bvs = (ArrayList<Long>) bvs.stream().distinct().collect(Collectors.toList());
			boundaryValueList.put(parameter, bvs);
		}

		makeInputDataList(_information);
	}

	public HashMap<String, ArrayList<Long>> getBoundaryValueList() { return boundaryValueList; }
	public ArrayList<HashMap<String, Long>> getInputDataList() { return inputDataList; }


	private void generateTypeBoundaryValue(InformationExtractor _information) {
		ArrayList<String> parameters = _information.getParameters();
		ArrayList<String> argumentTypes = _information.getArgumentTypes();

		for(int i=0; i<argumentTypes.size(); i++) {
			String parameter = parameters.get(i);
			String argumentType = argumentTypes.get(i);
			ArrayList<Long> bvs = boundaryValueList.get(parameter);
			switch (argumentType) {
				case "int":
					bvs.add(intMax + 1);
					bvs.add(intMax);
					bvs.add(intMin);
					bvs.add(intMin - 1);
					break;
				case "nat":
					bvs.add(natMax + 1);
					bvs.add(natMax);
					bvs.add(natMin);
					bvs.add(natMin - 1);
					break;
				case "nat1":
					bvs.add(nat1Max + 1);
					bvs.add(nat1Max);
					bvs.add(nat1Min);
					bvs.add(nat1Min - 1);
					break;
				default:
					break;
			}
		}
	}


	private void generateIfConditionalBoundaryValue(InformationExtractor _information) {
		HashMap<String, ArrayList<HashMap<String, String>>> allIfConditions = _information.getIfConditions();

		allIfConditions.forEach( (parameter, ifConditions) ->
                (ifConditions).forEach(condition -> {
                    //condition : HashMap<String, String>
                    String left = condition.get("left");
                    String operator = condition.get("operator");
                    String right = condition.get("right");
                    ArrayList<Long> bvs = boundaryValueList.get(parameter);

                    long trueValue = 0, falseValue = 0, value;
                    if (Util.isNumber(left)) {
                        value = Long.parseLong(left);
                        switch (operator) {
                            case "<":
                                trueValue = value + 1;
                                falseValue = value;
                                break;
                            case "<=":
                                trueValue = value;
                                falseValue = value - 1;
                                break;
                            case ">":
                                trueValue = value - 1;
                                falseValue = value;
                                break;
                            case ">=":
                                trueValue = value;
                                falseValue = value + 1;
                                break;
                            case "mod":
                                trueValue = value + Long.parseLong(condition.get("surplus"));
                                falseValue = value + 1;
                                bvs.add(value - 1);
                                break;
                        }

                    } else if (Util.isNumber(right)) {
                        value = Long.parseLong(right);
                        switch (operator) {
                            case "<":
                                trueValue = value - 1;
                                falseValue = value;
                                break;
                            case "<=":
                                trueValue = value;
                                falseValue = value + 1;
                                break;
                            case ">":
                                trueValue = value + 1;
                                falseValue = value;
                                break;
                            case ">=":
                                trueValue = value;
                                falseValue = value - 1;
                                break;
                            case "mod":
                                trueValue = value + Long.parseLong(condition.get("surplus"));
                                falseValue = value + 1;
                                bvs.add(value - 1);
                                break;
                        }
                    }

                    bvs.add(trueValue);
                    bvs.add(falseValue);

                }));
	}

	private void makeInputDataList(InformationExtractor _information) {
		ArrayList<String> parameters = _information.getParameters();

		//最初の一つ目
		String first_prm = parameters.get(0);
		ArrayList<Long> first_bvs = boundaryValueList.get(first_prm);
		for(int i=0; i<first_bvs.size(); i++) {
			inputDataList.add(new HashMap<>());
			HashMap<String, Long> hm = inputDataList.get(i);
			hm.put(first_prm, first_bvs.get(i));
		}

		//それ以降
		parameters.forEach(p -> {
			if( !p.equals(first_prm) ) { //最初の要素以外に対して
				ArrayList<Long> current_bvs = boundaryValueList.get(p);

				//inputDataListの第一引数のみを登録した状態
				ArrayList<HashMap<String, Long>> inputDataListInitialState = new ArrayList<>(inputDataList);

				for(int i=0; i<current_bvs.size() - 1; i++) {
					ArrayList<HashMap<String, Long>> inputDataListTmp = new ArrayList<>();
					inputDataListInitialState.forEach(inputDataOriginal -> {
						//inputDataを複製
						HashMap<String, Long> inputData = new HashMap<>();
						inputDataOriginal.forEach(inputData::put);
						inputDataListTmp.add(inputData);
					});
					inputDataList.addAll(inputDataListTmp);
				}
				int repeatTimesOfInsert = 0;
				for (Object current_bv : current_bvs) {
					long currentBv = (long) current_bv;
					int offset = repeatTimesOfInsert * inputDataListInitialState.size();
					for (int k = 0; k < inputDataListInitialState.size(); k++) {
						HashMap<String, Long> inputData = inputDataList.get(k + offset);
						inputData.put(p, currentBv);
					}
					repeatTimesOfInsert++;
				}

			}
		});
	} //end makeInputDatList

}

package bwdm.informationStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IfElseExprSyntaxTree {

	IfNode root;
	List<String> ifElses;
	private int count = 0;


	public IfElseExprSyntaxTree(String _ifExpressionBoby){
		shapeIfElseBody(_ifExpressionBoby);
		generateIfElseSyntaxTree();
		//recursiveReturnNodeFind(root);
	} //end constructor

	public IfNode getRoot() { return root; }

	/*
     * shaping of passed ifElseBody
     */
	private void shapeIfElseBody(String _ifElseBody) {
		ifElses = new ArrayList<>();
		String ifElseBody = _ifElseBody;
		ifElseBody = ifElseBody.replace("(", "").
				                replace(")", "").
				                replace("if", "if\n").
				                replace("else", "else\n").
				                replace("then", "").
				                replace(" ", "");
		ifElseBody = ifElseBody + "\n;";
		ifElses = Arrays.asList(ifElseBody.split("\n"));
	}


	//if式構文木を作る
	private void generateIfElseSyntaxTree() {
		//rootの準備
		count++; //最初はifなので無視
		String currentLine = ifElses.get(count++); //これは最初のifの条件式
		//多分
		root = generateIfNode(currentLine, null, 0);
		root.isIfNode = true;
		root.isTrueNode = null;
		//これだけでおｋ
	}

	private IfNode generateIfNode(String _condition, IfNode _parentNode, int _nodeLevel) {
		IfNode ifNode = new IfNode(_condition, _nodeLevel);
		ifNode.parentNode = _parentNode;
		String nextToken;

		//trueNode
		nextToken = ifElses.get(count++);
		if(nextToken.equals("if")){//ifの場合、次のtokenは条件式なので読み込む
			String conditionStr = ifElses.get(count++);
			ifNode.conditionTrueNode = generateIfNode(conditionStr, ifNode, _nodeLevel+1);
		} else {//ifじゃない場合、nextTokenにはreturnが入っている
			ifNode.conditionTrueNode = generateReturnNode(nextToken, ifNode, _nodeLevel+1);
		}
		ifNode.conditionTrueNode.isTrueNode = true;

		//else 特にすることは無いので無視
		//ということはif_elseファイルからelseを消しても問題無し？
		//nextToken = ifElses.get(count++);

		//elseの次、falseNode
		//falseNode
		nextToken = ifElses.get(count++);
		if(nextToken.equals("if")){//ifの場合、次のtokenは条件式なので読み込む
			String conditionStr = ifElses.get(count++);
			ifNode.conditionFalseNode = generateIfNode(conditionStr, ifNode, _nodeLevel+1);
		} else {//ifじゃない場合、nextTokenにはreturnが入っている
			ifNode.conditionFalseNode = generateReturnNode(nextToken, ifNode, _nodeLevel+1);
		}
		ifNode.conditionFalseNode.isTrueNode = false;

		return ifNode;
	}

	private ReturnNode generateReturnNode(String returnStr,
										  Node parentNode,
										  int _nodeLevel){
		ReturnNode returnNode =  new ReturnNode(returnStr, _nodeLevel);
		returnNode.parentNode = parentNode;
		return returnNode;
	}
}

package com.github.korosuke613.bwdm.informationStore

import java.util.*

class IfElseExprSyntaxTree(_ifExpressionBoby: String) {

    lateinit var root: IfNode
        internal set
    internal lateinit var ifElses: MutableList<String>
    private var count = 0


    init {
        shapeIfElseBody(_ifExpressionBoby)
        generateIfElseSyntaxTree()
        //recursiveReturnNodeFind(root);
    } //end constructor

    /*
     * shaping of passed ifElseBody
     */
    private fun shapeIfElseBody(_ifElseBody: String) {
        ifElses = ArrayList()
        var ifElseBody = _ifElseBody
        ifElseBody = ifElseBody.replace("(", "").replace(")", "").replace("if", "if\n").replace("else", "else\n").replace("then", "").replace("\n\n", "\n")
        ifElseBody = "$ifElseBody\n;"
        ifElses = mutableListOf(*ifElseBody.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        ifElses.forEachIndexed { index, it ->
            ifElses[index] = it.trim()
            if (it.contains("""\s+(if|else|elseif)\s*""".toRegex()) ||
                    it.contains("""\s*(if|else|elseif)\s+""".toRegex())) {
                ifElses[index] = it.replace(" ", "")
            }
        }
    }


    //if式構文木を作る
    private fun generateIfElseSyntaxTree() {
        //rootの準備
        if (ifElses[0] != "if") {
            throw NotIfNodeException("if文ではありません")
        }
        count++ //最初はifなので無視
        val currentLine = ifElses[count++] //これは最初のifの条件式
        //多分
        root = generateIfNode(currentLine, null, 0)
        root.isIfNode = true
        root.isTrueNode = null
        //これだけでおｋ
    }

    private fun generateIfNode(_condition: String, _parentNode: IfNode?, _nodeLevel: Int): IfNode {
        val ifNode = IfNode(_condition, _nodeLevel)
        ifNode.parentNode = _parentNode
        var nextToken: String = ifElses[count++]

        //trueNode
        if (nextToken == "if") {//ifの場合、次のtokenは条件式なので読み込む
            val conditionStr = ifElses[count++]
            ifNode.conditionTrueNode = generateIfNode(conditionStr, ifNode, _nodeLevel + 1)
        } else {//ifじゃない場合、nextTokenにはreturnが入っている
            ifNode.conditionTrueNode = generateReturnNode(nextToken, ifNode, _nodeLevel + 1)
        }
        ifNode.conditionTrueNode!!.isTrueNode = true

        //else 特にすることは無いので無視
        //ということはif_elseファイルからelseを消しても問題無し？
        count++

        //elseの次、falseNode
        //falseNode
        nextToken = ifElses[count++]
        if (nextToken == "if") {//ifの場合、次のtokenは条件式なので読み込む
            val conditionStr = ifElses[count++]
            ifNode.conditionFalseNode = generateIfNode(conditionStr, ifNode, _nodeLevel + 1)
        } else {//ifじゃない場合、nextTokenにはreturnが入っている
            ifNode.conditionFalseNode = generateReturnNode(nextToken, ifNode, _nodeLevel + 1)
        }
        ifNode.conditionFalseNode!!.isTrueNode = false

        return ifNode
    }

    private fun generateReturnNode(returnStr: String,
                                   parentNode: Node,
                                   _nodeLevel: Int): ReturnNode {
        val returnNode = ReturnNode(returnStr, _nodeLevel)
        returnNode.parentNode = parentNode
        return returnNode
    }
}

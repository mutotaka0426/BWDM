package bwdm.informationStore

class IfNode(private val conditionStr: String, _nodeLevel: Int) : Node() {
    var conditionTrueNode: Node? = null

    var conditionFalseNode: Node? = null //Ifノードには子ノードが2つ

    init {
        nodeLevel = _nodeLevel
        isIfNode = true
        ID = Node.staticID++
    }


    override fun getConditionOrReturnStr(): String {
        return conditionStr
    }

}

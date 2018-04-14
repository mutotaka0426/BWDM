package bwdm.informationStore

class IfNode(conditionStr: String, _nodeLevel: Int) : Node() {
    var conditionTrueNode: Node? = null

    var conditionFalseNode: Node? = null //Ifノードには子ノードが2つ
    override val conditionOrReturnStr = conditionStr

    init {
        nodeLevel = _nodeLevel
        isIfNode = true
        ID = Node.staticID++
    }
}

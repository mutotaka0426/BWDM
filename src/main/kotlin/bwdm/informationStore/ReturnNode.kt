package bwdm.informationStore

class ReturnNode internal constructor(returnStr: String, _nodeLevel: Int) : Node() {
    override val conditionOrReturnStr = returnStr

    init {
        nodeLevel = _nodeLevel
        isIfNode = false
        ID = Node.staticID++
    }
}

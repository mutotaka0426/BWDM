package bwdm.informationStore

class ReturnNode internal constructor(returnStr: String, _nodeLevel: Int) : Node() {
    override val conditionOrReturnStr = returnStr
    override var isIfNode = false

    init {
        nodeLevel = _nodeLevel
        ID = Node.staticID++
    }
}

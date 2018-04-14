package bwdm.informationStore

class ReturnNode internal constructor(private val returnStr: String, _nodeLevel: Int) : Node() {

    init {
        nodeLevel = _nodeLevel
        isIfNode = false
        ID = Node.staticID++
    }

    override fun getConditionOrReturnStr(): String {
        return returnStr
    }
}

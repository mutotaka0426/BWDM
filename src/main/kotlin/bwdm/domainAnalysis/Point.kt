package bwdm.domainAnalysis

data class Point(val name: String,
                 val type: String,
                 val bools: ArrayList<Boolean>,
                 val factors: HashMap<String, Int> = HashMap()){
    val inputData: java.util.HashMap<String, Long>
        get(){
            val hm = HashMap<String, Long>()
            for ((k, v) in factors){
                hm[k] = v.toLong()
            }
            return hm
        }
}

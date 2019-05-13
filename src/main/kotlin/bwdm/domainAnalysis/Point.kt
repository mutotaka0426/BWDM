package bwdm.domainAnalysis

data class Point(val name: String,
                 val type: String,
                 val bools: ArrayList<Boolean>,
                 val factors: HashMap<String, Int> = HashMap())

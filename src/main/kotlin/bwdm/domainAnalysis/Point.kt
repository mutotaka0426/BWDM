package bwdm.domainAnalysis

data class Point(val name: String,
                 val type: String,
                 val bools: ArrayList<Boolean> = ArrayList(),
                 val factors: HashMap<String, Factor> = HashMap())

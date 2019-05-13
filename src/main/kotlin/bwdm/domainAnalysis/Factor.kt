package bwdm.domainAnalysis

data class DomainPoints(val name: String){
    val onPoints:HashMap<String, Point> = HashMap()
    val offPoints:HashMap<String, Point> = HashMap()
    val inPoints:HashMap<String, Point> = HashMap()
    val outPoints:HashMap<String, Point> = HashMap()
}
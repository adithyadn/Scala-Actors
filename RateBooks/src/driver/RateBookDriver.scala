def sortResultMap(resultMap: Map[String, Map[String, Int]]) = {
  resultMap.toList.sortBy(_._2.values)
}

def printResultMap(resultMap: List[(String, Map[String, Int])]) = {
  resultMap.foreach{ isbn =>
    val resMap = isbn._2.iterator
    resMap.foreach(result => println(result._2 + "\t"+ isbn._1 +"\t"+ result._1 ))
  }
}

def timeAndPrint(name: String) (rateBooksCodeBlock: () => List[Map[String, Map[String, Int]]]) = {
  val startTime = System.currentTimeMillis()
  val resultMap = sortResultMap(rateBooksCodeBlock().flatten.toMap)
  val stopTime = System.currentTimeMillis()
  printResultMap(resultMap)
  println("Time taken for "+name+" solution in seconds = "+ (stopTime - startTime) / 1000.0 + "\n")
}

val rateBooks = new RateBooks("....", "...", "ecs.amazonaws.com")
val isbnList = scala.io.Source.fromFile( "RateBooks/isbn.txt" ).getLines().toList
timeAndPrint("sequential") { () => rateBooks.rateBooksSequential(isbnList)}
timeAndPrint("parallel") { () => rateBooks.rateBooksParallel(isbnList) }

package wordCounter

/**
 * Created by TeiKou on 27/11/2016.
 * Find common populate words in top 10 of each sources
 */
object wordCounterOrg {


  def main(args: Array[String]): Unit = {


    val t0 = System.nanoTime()
    //println(getCounter(getwcl("resources/file.txt"),"Scala"))
    val src1 = findMostPopularWord(getwcl("resources/file1.txt"))
    val src2 = findMostPopularWord(getwcl("resources/file2.txt"))
    src1.foreach(print(_))
    println()
    src2.foreach(print(_))
    println()
    println(src1.keySet.intersect(src2.keySet))

    val t1 = System.nanoTime()
    println("\n----------------------------------------Analyze end. Elapsed time: " + (t1 - t0) / 1000000000.0 + " s")


  }

  def getwcl(src: String) :Map[String,Int]= {

      scala.io.Source.fromFile(src)
        .getLines
        .flatMap(_.split("\\W+"))
        .foldLeft(Map.empty[String, Int]) {
        (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
      }
  }

  def findMostPopularWord(wl: Map[String,Int]) :Map[String,Int] ={
      wl.toSeq.sortWith(_._2 > _._2).take(10).toMap
  }

  def getCounter(wl:Map[String,Int],w:String) :Int={
    wl.getOrElse(w,0)
  }


}

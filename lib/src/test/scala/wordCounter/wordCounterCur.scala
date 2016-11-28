package wordCounter

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.util.{Success,Failure}
import ExecutionContext.Implicits.global

/**
 * Created by TeiKou on 27/11/2016.
 * Find common populate words in top 10 of each sources
 */
object wordCounterCur {


  def main(args: Array[String]): Unit = {


    val t0 = System.nanoTime()
    //println(getCounter(getwcl("resources/file.txt"),"Scala"))
    val src1 :Future[Map[String,Int]]= Future {findMostPopularWord(getwcl("resources/file1.txt"))}

    val src2 :Future[Map[String,Int]]= Future {findMostPopularWord(getwcl("resources/file2.txt"))}
    val rst1 = mutable.HashMap[String, Int]()
    val rst2 = mutable.HashMap[String, Int]()


    println()
    Await.ready(src1,Duration.Inf)

    src1 onComplete {
      case Success(rst) => {
        rst.foreach{
          print(_)
          rst1.+= _
        }
      }
      }
    Await.ready(src2,Duration.Inf)


    println()
    src1 onComplete {
      case Success(rst) => {
        rst.foreach{
          print(_)
          rst2.+= _
        }
      }
    }
    println()

    println(rst1.keySet.intersect(rst2.keySet))

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

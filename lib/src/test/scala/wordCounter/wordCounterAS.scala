package wordCounter

import cell.{Cell, CellCompleter, HandlerPool}
import lattice.{MapStringIntLattice, SetStringLattice, Lattice, DefaultKey}

import scala.concurrent.duration._

import scala.concurrent.{Promise, ExecutionContext, Await, Future}
import scala.util.{Success,Failure}

/**
 * Created by TeiKou on 27/11/2016.
 * Find common populate words in top 10 of each sources
 */
object wordCounterAS {

  /* lattice instance for cells */
  implicit val SetStringLattice: Lattice[Map[String,Int]] = new MapStringIntLattice
  type K = DefaultKey[Map[String,Int]]


  def main(args: Array[String]): Unit = {
    implicit val pool = new HandlerPool

    val t0 = System.nanoTime()
    //println(getCounter(getwcl("resources/file.txt"),"Scala"))
    //var src1:Map[String,Int]=Map.empty[String, Int]
    //var src2:Map[String,Int]=Map.empty[String, Int]

//pool.execute { () =>
  val src1 = findMostPopularWord(getwcl("resources/file1.txt").getResult())
  val src2 = findMostPopularWord(getwcl("resources/file2.txt").getResult())
//}



        src1.getResult().foreach(print(_))
        println()
        src2.getResult().foreach(print(_))
        println()
        println(src1.getResult().keySet.intersect(src2.getResult().keySet))
        val t1 = System.nanoTime()
        println("\n----------------------------------------Analyze end. Elapsed time: " + (t1 - t0) / 1000000000.0 + " s")

  }

  def getwcl(src: String)(implicit pool: HandlerPool) :Cell[K,Map[String,Int]]= {

    val resultLst = CellCompleter[K, Map[String,Int]](pool, new DefaultKey[Map[String,Int]])
    pool.execute { () =>
      val lrst: Map[String, Int] = {
        scala.io.Source.fromFile(src)
          .getLines
          .flatMap(_.split("\\W+"))
          .foldLeft(Map.empty[String, Int]) {
          (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
        }
      }
      resultLst.putNext(lrst)
    }
    waitUntilQuiescent(pool)

    resultLst.cell
  }

  def findMostPopularWord(wl: Map[String,Int])(implicit pool: HandlerPool)  :Cell[K,Map[String,Int]] ={
    val mplst = CellCompleter[K, Map[String, Int]](pool, new DefaultKey[Map[String, Int]])

    pool.execute { () =>
      mplst.putNext(wl.toSeq.sortWith(_._2 > _._2).take(10).toMap)
    }

    waitUntilQuiescent(pool)
    mplst.cell
  }

/*
  def getCounter(wl:Map[String,Int],w:String) :Int={
    wl.getOrElse(w,0)
  }
*/
def waitUntilQuiescent(pool: HandlerPool): Unit = {
  val p = Promise[Boolean]
  pool.onQuiescent { () =>
    p.success(true)
  }
  Await.ready(p.future, 30.seconds)
}

}

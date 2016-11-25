import cell.{Cell, CellCompleter, HandlerPool}
import lattice._
import org.apache.log4j.{Level, Logger}
import org.apache.spark._

// import classes required for using GraphX
import org.apache.spark.graphx._

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Promise, ExecutionContext, Future}

/**
 * Created by TeiKou on 24/11/2016.
 */
object flightInfoAlyAS {
  /* lattice instance for cells */
  implicit val SetStringLattice: Lattice[Set[String]] = new SetStringLattice
  type K = DefaultKey[Set[String]]




  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)

    // function to parse input into Flight class
    def parseFlight(str: String): Flight = {
      val line = str.split(",")
      Flight(line(0), line(1), line(2), line(3), line(4).toInt,
        line(5).toLong, line(6), line(7).toLong, line(8), line(9).toDouble, line(10).toDouble,
        line(11).toDouble, line(12).toDouble, line(13).toDouble, line(14).toDouble, line(15).toDouble, line(16).toInt)
    }
    //initialised your SparkContext
    // 1. Create Spark configuration
    val conf = new SparkConf().setAppName("MyInfoAnaly").setMaster("local[*]")
    // 2. Create Spark context
    val sc = new SparkContext(conf)

    // load the data into a RDD
    val textRDD = sc.textFile("resources/rita2014jan.csv")

    // parse the RDD of csv lines into an RDD of flight classes
    val flightsRDD = textRDD.map(parseFlight).cache()
    val airports = flightsRDD.map(flight => (flight.org_id, flight.origin)).distinct
    // Map airport ID to the 3-letter code to use for printlns
    val airportMap = airports.map { case ((org_id), name) => (org_id -> name) }.collect.toList.toMap
    val airportMap2 = airports.map { case ((org_id), name) => (name -> org_id) }.collect.toList.toMap

    // Defining a default vertex called nowhere
    val nowhere = "nowhere"


    // create routes RDD with srcid, destid, distance
    val routes = flightsRDD.map(flight => ((flight.org_id, flight.dest_id), flight.dist)).distinct

    // create edges RDD with srcid, destid , distance
    val edges = routes.map {
      case ((org_id, dest_id), distance) => Edge(org_id.toLong, dest_id.toLong, distance)
    }


    // define the graph
    val graph = Graph(airports, edges, nowhere)


    //val airportsIDs: ConcurrentSet[Int] = TrieMap.empty[Int, Int]
    type ConcurrentSet[T] = TrieMap[T, Int]

    //val resultFlights2 = CellCompleter(pool)

    //[App1] Find all flights from A to B which is direct or transfer for only 1 time and the distance should
    //not exceed 50% of the direct one
    //bookingTick("SFO","DFW")
    implicit val pool = new HandlerPool

    bookingTick("SFO","ORD")
    //SFO[14771]--ORD[13930],SFO[14771]--DFW[11298]--ORD[13930],
    //bookingTick("ORD","DFW")







    def bookingTick(startP:String,endP:String)(implicit pool: HandlerPool) {
      val t0 = System.nanoTime()
      pool.execute { () =>
        val startPId = airportMap2(startP)
        val endPId = airportMap2(endP)
        println("Finding flights from " + startP + "[" + startPId + "] to " + endP + "[" + endPId + "]")
        //val resultFlights = mutable.HashMap[List[Long], Int]()
        //val resultFlights = TrieMap[List[Long], Int]()
        val resultFlights = CellCompleter[K, Set[String]](pool, new DefaultKey[Set[String]])



        graph.edges.filter { case Edge(src1, dst1, prop1) => src1 == startPId }.collect.foreach {
          case Edge(src1, dst1, prop1) =>
            if (dst1 == endPId) {
              resultFlights.putNext(Set(src1 + "," + dst1 + "," + prop1))
            }
            else {
              val f = Future {
                graph.edges.filter { case Edge(src2, dst2, prop2) => {
                  src2 == dst1 && dst2 == endPId
                }
                }.collect.foreach {
                  case Edge(src2, dst2, prop2) => {
                    resultFlights.putNext(Set(src1 + "," + src2 + "," + dst2 + "," + (prop1 + prop2)))
                  }
                }
              }
              //Await.result(f,Duration.Inf)

            }


        }


        pool.onQuiescent {
          () => {
            resultFlights.cell.getResult().foreach {
              case (info) => println("Route: " + info)
            }
            val t1 = System.nanoTime()
            println("----------------------------------------Analyze Flight Info end. Elapsed time: " + (t1 - t0) / 1000000000.0 + " s")
          }

        }


        //resultFlights.cell
      }
      }


  }

}

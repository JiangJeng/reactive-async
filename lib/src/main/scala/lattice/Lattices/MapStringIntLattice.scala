package lattice



object MapStringIntKey extends DefaultKey[Map[String,Int]]

class MapStringIntLattice extends Lattice[Map[String,Int]] {
  override def join(current: Map[String,Int], next: Map[String,Int]): Map[String,Int] = {
    current ++ next.map{ case (k,v) => k -> (v + current.getOrElse(k,0)) }
    /*
    current.foldLeft(Map.empty[String, Int]) {
      (acc, kv) => acc + (kv._1 -> (kv._2+ (acc.getOrElse(kv._1, 0) + 1)))
    }
    */
  }

  override def empty: Map[String,Int]= Map.empty[String, Int]
}

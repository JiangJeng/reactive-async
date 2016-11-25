package lattice


object SetStringKey extends DefaultKey[Set[String]]

class SetStringLattice extends Lattice[Set[String]] {
  override def join(current: Set[String], next: Set[String]): Set[String] = {
    next ++ current
  }

  override def empty: Set[String] = Set.empty
}

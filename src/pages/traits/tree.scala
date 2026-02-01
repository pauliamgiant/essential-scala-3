enum Tree:
  case Node(l: Tree, r: Tree)
  case Leaf(elt: Int)
  
  def sum: Int = this match
    case Leaf(elt) => elt
    case Node(l, r) => l.sum + r.sum
  
  def double: Tree = this match
    case Leaf(elt) => Leaf(elt * 2)
    case Node(l, r) => Node(l.double, r.double)

object TreeOps:
  def sum(tree: Tree): Int =
    tree match
      case Tree.Leaf(elt) => elt
      case Tree.Node(l, r) => sum(l) + sum(r)

  def double(tree: Tree): Tree =
    tree match
      case Tree.Leaf(elt) => Tree.Leaf(elt * 2)
      case Tree.Node(l, r) => Tree.Node(double(l), double(r))

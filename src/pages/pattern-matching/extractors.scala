object Positive:
  def unapply(in: Int): Option[Int] =
    if in > 0 then
      Some(in)
    else
      None

def main(args: Array[String]): Unit =
  assert(
    "No" ==
      (0 match
        case Positive(_) => "Yes"
        case _           => "No"),
  )

  assert(
    "Yes" ==
      (42 match
        case Positive(_) => "Yes"
        case _           => "No"),
  )

object Titlecase:
  def unapply(str: String): Option[String] =
    Some(str.split(" ").toList.map {
      case ""   => ""
      case word => word.substring(0, 1).toUpperCase + word.substring(1)
    }.mkString(" "))

end main
def test1(): Unit =
  assert(
    "Sir Lord Doctor David Gurnell" ==
      ("sir lord doctor david gurnell" match
        case Titlecase(str) => str),
  )

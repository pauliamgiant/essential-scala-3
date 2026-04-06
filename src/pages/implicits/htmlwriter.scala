case class Person(name: String, email: String)

trait HtmlWriter[T]:
  def write(in: T): String
object HtmlWriter:
  def apply[A](using writer: HtmlWriter[A]): HtmlWriter[A] =
    writer

given PersonWriter: HtmlWriter[Person] with
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"

object HtmlUtil:
  def htmlify[T](data: T)(using writer: HtmlWriter[T]): String =
    writer.write(data)

given ApproximationWriter: HtmlWriter[Int] with
  def write(in: Int): String =
    s"It's definitely less than ${((in / 10) + 1) * 10}"

def main(args: Array[String]): Unit =
  println(HtmlUtil.htmlify(2))
  println(HtmlWriter[Person].write(Person("Noel", "noel@example.org")))
end main

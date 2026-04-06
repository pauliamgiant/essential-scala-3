## Type Enrichment Using Extension Methods

A second approach to creating type class interfaces, called *type enrichment*[^pimping], allows us to create interfaces that act as if they were methods defined on the classes of interest. For example, suppose we have a method called `numberOfVowels`:

```scala mdoc:silent
def numberOfVowels(str: String) =
  str.filter(Seq('a', 'e', 'i', 'o', 'u').contains(_)).length
```

```scala mdoc
numberOfVowels("the quick brown fox")
```

[^pimping]: Type enrichment is sometimes referred to as pimping in older literature. We will not use that term.

This is a method that we use all the time. It would be great if `numberOfVowels` was a built-in method of `String` so we could write `"a string".numberOfVowels`, but of course we can't change the source code for `String`. Scala 3 has a feature called *extension methods* that allow us to add new functionality to an existing class without editing its source code. This allows us to extend any type with new methods.

### Extension Methods

Let's build up extension methods piece by piece. We can use the `extension` keyword to add an `numberOfVowels` method to `String`:

```scala mdoc:reset-object:silent
val vowels = Seq('a', 'e', 'i', 'o', 'u')

extension (str: String)
  def numberOfVowels =
    str.toList.filter(vowels.contains(_)).length
```

We can now use this method directly on any `String`:

```scala mdoc:reset-object:silent
val vowels = Seq('a', 'e', 'i', 'o', 'u')

extension (str: String)
  def numberOfVowels =
    str.toList.filter(vowels.contains(_)).length
```

```scala mdoc
"the quick brown fox".numberOfVowels
```

When the compiler processes our call to `numberOfVowels`, it looks for an extension method that provides the method for a `String`. It finds our extension and uses it, allowing our code to type check correctly.

Extension methods follow the same scoping rules: they must be defined within an enclosing object, class, or trait, or imported into the current scope where needed.

## Combining Type Classes and Type Enrichment

Extension methods can be used on their own but we most often combine them with type classes to create a more natural style of interface. We keep the type class (`HtmlWriter`) and adapters, and add extension methods that themselves take using parameters. For example:

```scala mdoc:invisible
trait HtmlWriter[A]:
  def toHtml(a: A): String

case class Person(name: String, email: String)
given PersonWriter: HtmlWriter[Person] with
  def toHtml(person: Person) =
    s"${person.name} (${person.email})"
```

```scala mdoc:silent
extension [T](data: T)
  def toHtml(using writer: HtmlWriter[T]) =
    writer.toHtml(data)
```

This allows us to invoke our type-class pattern on any type for which we have an adapter *as if it were a built-in feature of the class*:

```scala mdoc
Person("John", "john@example.com").toHtml
```

This gives us many benefits. We can extend existing types to give them new functionality, use simple syntax to invoke the functionality, *and* choose our preferred implementation by controlling which givens we have in scope.

### Take Home Points

*Extension methods* are a Scala language feature that allows us to define extra functionality on existing data types without using conventional inheritance. This is a programming pattern called *type enrichment*.

The Scala compiler uses extension methods to provide additional methods on types. Extension methods integrate seamlessly with the Scala language and allow us to extend any type with new functionality.

Extension methods can be organized in objects and brought into scope through imports as needed.

### Exercises

#### Drinking the Kool Aid

Use extension methods to add a method `yeah` to `Int`, which prints `Oh yeah!` as many times as the `Int` on which it is called if the `Int` is positive, and is silent otherwise. Here's an example of usage:

```scala
2.yeah()

3.yeah()

-1.yeah()

```

When you have written your extension method, package it in an `IntExtensions` object.

<div class="solution">
```scala mdoc:silent
object IntExtensions:
  extension (n: Int)
    def yeah() = for _ <- 0 until n do println("Oh yeah!")

import IntExtensions.*
```

```scala mdoc
2.yeah()
```

The solution uses a `for` comprehension and a range to iterate through the correct number of iterations. Remember that the range `0 until n` is the same as `0 to n-1`---it contains all numbers from `0` inclusive to `n` exclusive.
</div>

#### Times

Extend your previous example to give `Int` an extra method called `times` that accepts a function of type `Int => Unit` as an argument and executes it `n` times. Example usage:

```scala
3.times(i => println(s"Look - it's the number $i!"))
```

For bonus points, re-implement `yeah` in terms of `times`.

<div class="solution">
```scala mdoc:nest:silent
object IntExtensions:
  extension (n: Int)
    def yeah(): Unit =
      times(_ => println("Oh yeah!"))

    def times(func: Int => Unit): Unit =
      for i <- 0 until n do func(i)
```
</div>
      for i <- 0 until n do func(i)
```
</div>

### Easy Equality

Recall our `Equal` type class from a previous section.

```scala mdoc:silent
trait Equal[A]:
  def equal(v1: A, v2: A): Boolean
```

Implement an enrichment so we can use this type class via a triple equal (`===`) method. For example, if the correct implicits are in scope the following should work.

```scala
"abcd".===("ABCD") // Assumes case-insensitive equality implicit
```

<div class="solution">
We just need to define an implicit class, which I have here placed in the companion object of `Equal`.

```scala mdoc:nest:silent
trait Equal[A]:
  def equal(v1: A, v2: A): Boolean

object Equal:
  def apply[A](implicit instance: Equal[A]): Equal[A] =
    instance

  implicit class ToEqual[A](in: A):
    def ===(other: A)(implicit equal: Equal[A]): Boolean =
      equal.equal(in, other)
```

Here is an example of use.

```scala mdoc:silent
implicit val caseInsensitiveEquals = new Equal[String]:
  def equal(s1: String, s2: String) =
    s1.toLowerCase == s2.toLowerCase

import Equal.*

"foo".===("FOO")
```
</div>
